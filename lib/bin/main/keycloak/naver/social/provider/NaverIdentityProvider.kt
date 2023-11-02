package keycloak.naver.social.provider

import com.fasterxml.jackson.databind.JsonNode;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.models.KeycloakSession;

class NaverIdentityProvider(session: KeycloakSession, config: OAuth2IdentityProviderConfig) :
    AbstractOAuth2IdentityProvider<OAuth2IdentityProviderConfig>(session, config),
    SocialIdentityProvider<OAuth2IdentityProviderConfig> {

    init {
        config.authorizationUrl = AUTH_URL
        config.tokenUrl = TOKEN_URL
        config.userInfoUrl = PROFILE_URL
    }

    companion object {
        const val AUTH_URL = "https://nid.naver.com/oauth2.0/authorize"
        const val TOKEN_URL = "https://nid.naver.com/oauth2.0/token"
        const val PROFILE_URL =  "https://openapi.naver.com/v1/nid/me"
        const val DEFAULT_SCOPE = "profile"
    }

    override fun getDefaultScopes(): String {
        return DEFAULT_SCOPE
    }

    override fun doGetFederatedIdentity(accessToken: String): BrokeredIdentityContext {
        try {
            val profile = fetchUserProfile(accessToken)
            val id = getJsonProperty(profile, "id")
            val name = getJsonProperty(profile, "name")
            val email = getJsonProperty(profile, "email")
            val user = BrokeredIdentityContext(id)
            user.username = email
            user.email = email
            user.idpConfig = config
            user.idp = this
            AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, config.alias)
            return user
        } catch (e: Exception) {
            println("doGetFederatedIdentity: $e")
            throw IdentityBrokerException("Could not obtain user profile from Naver.", e)
        }
    }

    private fun fetchUserProfile(accessToken: String): JsonNode {
        return SimpleHttp.doGet(PROFILE_URL, session)
            .header("Authorization", "Bearer $accessToken")
            .asJson()["response"]
            .also { println("fetchUserProfile: $it") }
    }
}
