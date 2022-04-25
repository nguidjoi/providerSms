package org.keycloak.examples.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.*;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.examples.authenticator.service.SmsService;
import org.keycloak.examples.authenticator.service.SmsServiceImp;
import org.keycloak.models.*;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.models.utils.TimeBasedOTP;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author <a href="mailto:alain.nguidjoi.bell@gmail.com"> Alain NGUIDJOI BELL</a>
 * @version $Revision: 1 $
 */
public class OtpSmsAuthenticator implements Authenticator, CredentialValidator<OtpSmsCredentialProvider> {

    private static final Logger logger = Logger.getLogger(OtpSmsAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (generateSaveAndSendCode(context)) {
            Response challenge = context.form()
                    .createForm("otp-code.ftl");
            context.challenge(challenge);
            return;
        }

        Response challenge = context.form()
                .setError("Unable to send sms code.")
                .createForm("otp-code.ftl");
        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
    }

    private boolean generateSaveAndSendCode(AuthenticationFlowContext context) {
        String value = generateCode(context);
        saveCode(context, value);

        UserModel user = context.getUser();
        String mobile = user.getFirstAttribute("mobile");

        SmsService service = new SmsServiceImp();
        service.send(mobile, value);
        logger.info("Generated value sent is : " + value);
        return true;
    }

    private String generateCode(AuthenticationFlowContext context) {
        OTPPolicy otpPolicy = context.getRealm().getOTPPolicy();
        TimeBasedOTP generator = new TimeBasedOTP(otpPolicy.getAlgorithm(), otpPolicy.getDigits(), otpPolicy.getPeriod(), otpPolicy.getLookAheadWindow());
        String secret = TimeBasedOTP.generateSecret(13);
        return generator.generateOTP(secret, "11", otpPolicy.getDigits(), otpPolicy.getAlgorithm());

    }

    @Override
    public void action(AuthenticationFlowContext context) {
        logger.info("Beginning action ");
        validateCode(context);
    }

    protected void validateCode(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst("otp_value");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote("otpCode");
        String period = authSession.getAuthNote("period");

        if (code == null || period == null) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
        }

        boolean isValidated = enteredCode.equals(code);
        if (isValidated) {
            if (Long.parseLong(period) < System.currentTimeMillis()) {
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                        context.form().setError("This code is expired").createErrorPage(Response.Status.BAD_REQUEST));
            } else {
                context.success();
            }
        } else {
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().setAttribute("realm", context.getRealm())
                                .setError("Invalid Otp code provided.").createForm("otp-code.ftl"));
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getFirstAttribute("mobile") != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {

    }

    @Override
    public OtpSmsCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (OtpSmsCredentialProvider) session.getProvider(CredentialProvider.class, OtpSmsCredentialProviderFactory.PROVIDER_ID);
    }

    public void saveCode(AuthenticationFlowContext context, String value) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int ttl = Integer.parseInt(config.getConfig().get("period"));
        authSession.setAuthNote("otpCode", value);
        authSession.setAuthNote("period", Long.toString(System.currentTimeMillis() + (ttl * 1000L)));
        context.success();
    }

}
