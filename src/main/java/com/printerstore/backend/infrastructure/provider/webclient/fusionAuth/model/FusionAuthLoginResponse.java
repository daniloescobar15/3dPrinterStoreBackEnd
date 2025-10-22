package com.printerstore.backend.infrastructure.provider.webclient.fusionAuth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FusionAuthLoginResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("tokenExpirationInstant")
    private Long tokenExpirationInstant;

    @JsonProperty("user")
    private UserData user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserData {
        @JsonProperty("active")
        private Boolean active;

        @JsonProperty("connectorId")
        private String connectorId;

        @JsonProperty("data")
        private Map<String, Object> data;

        @JsonProperty("email")
        private String email;

        @JsonProperty("id")
        private String id;

        @JsonProperty("identities")
        private List<Identity> identities;

        @JsonProperty("insertInstant")
        private Long insertInstant;

        @JsonProperty("lastLoginInstant")
        private Long lastLoginInstant;

        @JsonProperty("lastUpdateInstant")
        private Long lastUpdateInstant;

        @JsonProperty("memberships")
        private List<Object> memberships;

        @JsonProperty("passwordChangeRequired")
        private Boolean passwordChangeRequired;

        @JsonProperty("passwordLastUpdateInstant")
        private Long passwordLastUpdateInstant;

        @JsonProperty("preferredLanguages")
        private List<String> preferredLanguages;

        @JsonProperty("registrations")
        private List<Registration> registrations;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("twoFactor")
        private TwoFactor twoFactor;

        @JsonProperty("uniqueUsername")
        private String uniqueUsername;

        @JsonProperty("username")
        private String username;

        @JsonProperty("usernameStatus")
        private String usernameStatus;

        @JsonProperty("verified")
        private Boolean verified;

        @JsonProperty("verifiedInstant")
        private Long verifiedInstant;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Identity {
        @JsonProperty("insertInstant")
        private Long insertInstant;

        @JsonProperty("lastLoginInstant")
        private Long lastLoginInstant;

        @JsonProperty("lastUpdateInstant")
        private Long lastUpdateInstant;

        @JsonProperty("primary")
        private Boolean primary;

        @JsonProperty("type")
        private String type;

        @JsonProperty("value")
        private String value;

        @JsonProperty("verified")
        private Boolean verified;

        @JsonProperty("verifiedReason")
        private String verifiedReason;

        @JsonProperty("displayValue")
        private String displayValue;

        @JsonProperty("moderationStatus")
        private String moderationStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Registration {
        @JsonProperty("applicationId")
        private String applicationId;

        @JsonProperty("data")
        private Map<String, Object> data;

        @JsonProperty("id")
        private String id;

        @JsonProperty("insertInstant")
        private Long insertInstant;

        @JsonProperty("lastLoginInstant")
        private Long lastLoginInstant;

        @JsonProperty("lastUpdateInstant")
        private Long lastUpdateInstant;

        @JsonProperty("preferredLanguages")
        private List<String> preferredLanguages;

        @JsonProperty("roles")
        private List<String> roles;

        @JsonProperty("tokens")
        private Map<String, Object> tokens;

        @JsonProperty("username")
        private String username;

        @JsonProperty("usernameStatus")
        private String usernameStatus;

        @JsonProperty("verified")
        private Boolean verified;

        @JsonProperty("verifiedInstant")
        private Long verifiedInstant;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TwoFactor {
        @JsonProperty("methods")
        private List<Object> methods;

        @JsonProperty("recoveryCodes")
        private List<Object> recoveryCodes;
    }
}