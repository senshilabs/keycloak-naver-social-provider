package keycloak.naver.social.provider

import org.keycloak.broker.oidc.mappers.AbstractClaimMapper
import org.keycloak.broker.provider.BrokeredIdentityContext
import org.keycloak.broker.saml.mappers.UsernameTemplateMapper.*
import org.keycloak.broker.saml.mappers.UsernameTemplateMapper.Target
import org.keycloak.models.IdentityProviderMapperModel
import org.keycloak.models.KeycloakSession
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel
import org.keycloak.models.utils.KeycloakModelUtils
import org.keycloak.provider.ProviderConfigProperty
import java.util.function.UnaryOperator
import java.util.regex.Pattern


class NaverIdentityProviderMapper : AbstractClaimMapper() {
    companion object {
        const val PROVIDER_ID = "naver-user-attribute-mapper"
        private val SUBSTITUTION: Pattern = Pattern.compile("\\$\\{([^}]+?)(?:\\s*\\|\\s*(\\S+)\\s*)?\\}")

        val configProperties = listOf(
            ProviderConfigProperty().also {
                it.name = TEMPLATE
                it.label = "Template"
                it.type = ProviderConfigProperty.STRING_TYPE
                it.helpText = "The attribute name of the username in the Naver user profile."
                it.defaultValue = "\${ALIAS}.\${CLAIM.id}"
            },
            ProviderConfigProperty().also {
                it.name = TARGET
                it.label = "Target"
                it.helpText = "Destination field for the mapper. LOCAL (default) means that the changes are applied to the username stored in local database upon user import. BROKER_ID and BROKER_USERNAME means that the changes are stored into the ID or username used for federation user lookup, respectively."
                it.type = ProviderConfigProperty.LIST_TYPE
                it.options = TARGETS
                it.defaultValue = Target.LOCAL.toString()
            }
        )
    }

    override fun getId(): String {
        return PROVIDER_ID
    }

    override fun getHelpText(): String {
        return "Format the username to import."
    }

    override fun getConfigProperties(): MutableList<ProviderConfigProperty> {
        return NaverIdentityProviderMapper.configProperties.toMutableList()
    }

    override fun getCompatibleProviders(): Array<String> {
        return arrayOf(NaverIdentityProviderFactory.PROVIDER_ID)
    }

    override fun getDisplayCategory(): String {
        return "Preprocessor"
    }

    override fun getDisplayType(): String {
        return "Username Template Importer"
    }

    override fun updateBrokeredUserLegacy(
        session: KeycloakSession?,
        realm: RealmModel?,
        user: UserModel?,
        mapperModel: IdentityProviderMapperModel?,
        context: BrokeredIdentityContext?
    ) {
    }

    override fun updateBrokeredUser(
        session: KeycloakSession?,
        realm: RealmModel,
        user: UserModel,
        mapperModel: IdentityProviderMapperModel,
        context: BrokeredIdentityContext
    ) {
        if (getTarget(mapperModel.config[TARGET]) === Target.LOCAL && !realm.isRegistrationEmailAsUsername) {
            user.username = context.modelUsername
        }
    }

    override fun preprocessFederatedIdentity(
        session: KeycloakSession?,
        realm: RealmModel?,
        mapperModel: IdentityProviderMapperModel?,
        context: BrokeredIdentityContext?
    ) {
        if(mapperModel != null && context != null)
        {
            setUserNameFromTemplate(mapperModel, context)
        }
    }

    private fun setUserNameFromTemplate(mapperModel: IdentityProviderMapperModel, context: BrokeredIdentityContext)
    {
        val alias = context.idpConfig.alias
        val uuid = { KeycloakModelUtils.generateId() }

        val template = mapperModel.config[TEMPLATE] ?: ""

        val matcher = SUBSTITUTION.matcher(template)
        val sb = StringBuffer()

        while (matcher.find()) {
            val variable = matcher.group(1)
            val transformer: UnaryOperator<String> = TRANSFORMERS[matcher.group(2)] ?: UnaryOperator.identity()

            val replacement = when(variable) {
                "ALIAS" -> transformer.apply(alias)
                "UUID" -> transformer.apply(uuid())
                else -> {
                    if (variable.startsWith("CLAIM.")) {
                        val name = variable.substring("CLAIM.".length)
                        val value = AbstractClaimMapper.getClaimValue(context, name) ?: ""
                        transformer.apply(value.toString())
                    } else {
                        variable
                    }
                }
            }

            matcher.appendReplacement(sb, replacement)
        }

        matcher.appendTail(sb)

        val target  = getTarget(Target.LOCAL.toString())
        target.set(context,sb.toString())
    }

}