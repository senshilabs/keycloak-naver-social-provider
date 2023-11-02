package keycloak.naver.social.provider

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig
import org.keycloak.broker.provider.AbstractIdentityProviderFactory
import org.keycloak.broker.social.SocialIdentityProviderFactory
import org.keycloak.models.IdentityProviderModel
import org.keycloak.models.KeycloakSession

class NaverIdentityProviderFactory : AbstractIdentityProviderFactory<NaverIdentityProvider>(), SocialIdentityProviderFactory<NaverIdentityProvider> {

    companion object {
        const val PROVIDER_ID = "naver"
    }

    override fun getName(): String {
        return "Naver"
    }

    override fun create(session: KeycloakSession, model: IdentityProviderModel): NaverIdentityProvider {
        return NaverIdentityProvider(session, OAuth2IdentityProviderConfig(model))
    }

    override fun createConfig(): OAuth2IdentityProviderConfig {
        return OAuth2IdentityProviderConfig()
    }

    override fun getId(): String {
        return PROVIDER_ID
    }
}
