package keycloak.naver.social.provider

import org.keycloak.broker.oidc.mappers.AbstractClaimMapper
import org.keycloak.broker.oidc.mappers.UserAttributeMapper.*
import org.keycloak.broker.provider.BrokeredIdentityContext
import org.keycloak.broker.saml.mappers.UsernameTemplateMapper.*
import org.keycloak.models.*
import org.keycloak.provider.ProviderConfigProperty
import org.keycloak.utils.StringUtil
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors


class NaverUserAttributeMapper : AbstractClaimMapper() {

    companion object {
        const val PROVIDER_ID = "naver-user-attribute-mapper"
        private val IDENTITY_PROVIDER_SYNC_MODES: Set<IdentityProviderSyncMode> =
            HashSet(IdentityProviderSyncMode.entries)

        val configProperties = listOf(
            ProviderConfigProperty().also {
                it.name = CLAIM
                it.label = "Claim"
                it.helpText = "Name of claim to search for in token. You can reference nested claims using a '.', i.e. 'address.locality'. To use dot (.) literally, escape it with backslash (\\\\.)"
                it.type = ProviderConfigProperty.STRING_TYPE
            },
            ProviderConfigProperty().also {
                it.name = USER_ATTRIBUTE
                it.label = "User Attribute Name"
                it.helpText = "User attribute name to store claim.  Use email, lastName, and firstName to map to those predefined user properties."
                it.type = ProviderConfigProperty.STRING_TYPE
            }

        )
    }


    override fun supportsSyncMode(syncMode: IdentityProviderSyncMode?): Boolean {
        return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode)
    }

    override fun getId(): String {
        return PROVIDER_ID
    }

    override fun getHelpText(): String {
        return "Import declared claim if it exists in ID, access token or the claim set returned by the user profile endpoint into the specified user property or attribute.";
    }

    override fun getConfigProperties(): MutableList<ProviderConfigProperty> {
        return NaverUserAttributeMapper.configProperties.toMutableList()
    }

    override fun getCompatibleProviders(): Array<String> {
        return arrayOf(NaverIdentityProviderFactory.PROVIDER_ID)
    }

    override fun getDisplayCategory(): String {
        return "Attribute Importer"
    }

    override fun getDisplayType(): String {
        return "Attribute Importer"
    }

    override fun preprocessFederatedIdentity(
        session: KeycloakSession?,
        realm: RealmModel?,
        mapperModel: IdentityProviderMapperModel,
        context: BrokeredIdentityContext
    ) {
        val attribute = mapperModel.config[USER_ATTRIBUTE]
        val values = getClaimValues(mapperModel, context)

        if (attribute.isNullOrBlank() || values.isEmpty()) {
            return
        }

        setContextAttribute(attribute, context, values)
    }

    private fun setContextAttribute(
        attribute: String,
        context: BrokeredIdentityContext,
        values: List<String>
    ) {
        when (attribute.lowercase()) {
            EMAIL.lowercase() -> context.email = values[0]
            FIRST_NAME.lowercase() -> context.firstName = values[0]
            LAST_NAME.lowercase() -> context.lastName = values[0]
            else -> context.setUserAttribute(attribute, values)
        }
    }

    private fun getClaimValues(mapperModel: IdentityProviderMapperModel, context: BrokeredIdentityContext?): List<String> {
        return getClaimValue(mapperModel,context).let (::claimValuesToList)
    }
    private fun claimValuesToList(value: Any): List<String> {
        val values: List<Any?> = if (value is List<*>) value else listOf(value)
        return values.mapNotNull { obj: Any? -> obj?.toString() }
    }

    override fun updateBrokeredUser(
        session: KeycloakSession?,
        realm: RealmModel?,
        user: UserModel,
        mapperModel: IdentityProviderMapperModel,
        context: BrokeredIdentityContext?
    ) {

        val attribute = mapperModel.config[USER_ATTRIBUTE]
        val values = getClaimValues(mapperModel, context)

        if (attribute.isNullOrBlank() || context == null) {
            return
        }

        if (values.isNotEmpty()) {
            setContextAttribute(attribute, context, values)
        }
        else {
            val defaultAttribute = listOf(EMAIL, FIRST_NAME, LAST_NAME).map { it.lowercase() }
            if (!defaultAttribute.contains(attribute.lowercase())) {
                user.removeAttribute(attribute);
            }
        }
    }
}