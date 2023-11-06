# keycloak-naver-social-provider

![image](https://github.com/senshilabs/keycloak-naver-social-provider/assets/10369528/ee76898d-6b16-4eb3-b2f3-1d3d99745f2c)


![image](https://github.com/senshilabs/keycloak-naver-social-provider/assets/10369528/df2f31fe-e7ee-42fc-adac-7e5c98766648)

이 프로젝트는 Keycloak를 위한 네이버 소셜 프로바이더와 Attribute Mapper 를 제공합니다.

## 사용법
jar 파일을 다운로드 받아서 Keycloak의 `opt/keycloak/providers` 디렉토리에 복사하고, `build` 합니다.

```sh
sh-5.1$ ./kc.sh build
Updating the configuration and installing your custom providers, if any. Please wait.
2023-11-06 07:17:05,950 WARN  [org.keycloak.services] (build-41) KC-SERVICES0047: naver-username-template-mapper (keycloak.naver.social.provider.NaverUsernameTemplateMapper) is implementing the internal SPI identity-provider-mapper. This SPI is internal and may change without notice
2023-11-06 07:17:05,950 WARN  [org.keycloak.services] (build-41) KC-SERVICES0047: naver-user-attribute-mapper (keycloak.naver.social.provider.NaverUserAttributeMapper) is implementing the internal SPI identity-provider-mapper. This SPI is internal and may change without notice
2023-11-06 07:17:06,365 WARN  [org.keycloak.services] (build-41) KC-SERVICES0047: naver (keycloak.naver.social.provider.NaverIdentityProviderFactory) is implementing the internal SPI identity_provider. This SPI is internal and may change without notice
2023-11-06 07:17:13,013 INFO  [io.quarkus.deployment.QuarkusAugmentor] (main) Quarkus augmentation completed in 9086ms
Server configuration updated and persisted. Run the following command to review the configuration:
```


## 기타
해당 SPI 는 keycloak 22.0.5 버전에서 테스트 되었습니다.
