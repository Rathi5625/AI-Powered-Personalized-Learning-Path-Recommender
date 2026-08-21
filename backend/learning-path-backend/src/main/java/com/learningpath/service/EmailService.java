package com.learningpath.service;

import com.learningpath.entity.enums.OtpPurpose;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.from:noreply@learnai.com}")
    private String fromEmail;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.otp.dev-logging:false}")
    private boolean devLogging;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void initDiagnosticLog() {
        boolean smtpConfigured = mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();

        log.info("================================================================================");
        log.info("[EmailService] SMTP Diagnostic Status:");
        log.info("  - SMTP configured: {}", smtpConfigured);
        log.info("  - SMTP host: {}:{}", mailHost, mailPort);
        log.info("  - SMTP username configured: {}", (mailUsername != null && !mailUsername.isBlank()));
        log.info("  - SMTP password configured: {}", (mailPassword != null && !mailPassword.isBlank()));
        log.info("  - MAIL_FROM configured: {}", (fromEmail != null && !fromEmail.isBlank() ? fromEmail : "not configured"));
        log.info("  - JavaMailSender active: {}", (mailSender != null));
        log.info("  - OTP dev logging enabled: {}", devLogging);
        log.info("================================================================================");
    }

    public void sendOtpEmail(String toEmail, String otpCode, OtpPurpose purpose) {
        String subject = (purpose == OtpPurpose.EMAIL_VERIFICATION)
                ? "Your LearnAI Verification Code"
                : "Reset your LearnAI Password";

        String heading = (purpose == OtpPurpose.EMAIL_VERIFICATION)
                ? "Verify your email"
                : "Password Reset Request";

        String messageBody = (purpose == OtpPurpose.EMAIL_VERIFICATION)
                ? "Thank you for joining LearnAI. Please use the following 6-digit verification code to complete your signup:"
                : "We received a request to reset your LearnAI account password. Please use the following verification code:";

        if (devLogging) {
            log.info("================================================================================");
            log.info("[EMAIL SERVICE] [DEV_LOGGING_ACTIVE] OTP for email={} purpose={} is: [{}] (expires in {} mins)",
                    toEmail, purpose, otpCode, otpExpiryMinutes);
            log.info("================================================================================");
        }

        log.info("[EmailService] OTP email send attempt started for email={}, purpose={}, expires in {} mins",
                toEmail, purpose, otpExpiryMinutes);

        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            log.warn("[EmailService] SMTP not fully configured (mailSender={}, username configured={}). Real email delivery skipped.",
                    mailSender != null, (mailUsername != null && !mailUsername.isBlank()));
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            if (message == null) {
                log.warn("[EmailService] JavaMailSender returned null message. Skipping delivery.");
                return;
            }

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String sender = (fromEmail != null && !fromEmail.isBlank()) ? fromEmail : mailUsername;
            helper.setFrom(sender);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            String htmlContent = buildEmailTemplate(heading, messageBody, otpCode, otpExpiryMinutes);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[EmailService] OTP email send successful for email={}", toEmail);
        } catch (Exception ex) {
            log.error("[EmailService] OTP email send failed for email={}: {}", toEmail, ex.getMessage());
        }
    }

    private String buildEmailTemplate(String heading, String messageBody, String otpCode, int expiryMinutes) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f9f9ff; margin: 0; padding: 20px; color: #0f1b32; }
                        .container { max-width: 520px; margin: 0 auto; background: #ffffff; border-radius: 24px; padding: 32px; border: 1px solid #f0ebe7; box-shadow: 0 8px 30px rgba(0,0,0,0.04); }
                        .brand { font-size: 20px; font-weight: 800; color: #8e4d2b; margin-bottom: 24px; display: inline-block; }
                        .title { font-size: 22px; font-weight: 800; margin-bottom: 12px; color: #0f1b32; }
                        .text { font-size: 14px; line-height: 1.6; color: #53433c; margin-bottom: 24px; }
                        .otp-box { background: #faf4f0; border: 1.5px dashed #d98b63; border-radius: 16px; padding: 18px; text-align: center; margin-bottom: 24px; }
                        .otp-code { font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #8e4d2b; margin: 0; }
                        .footer { font-size: 12px; color: #86736b; line-height: 1.5; border-top: 1px solid #f0ebe7; padding-top: 20px; margin-top: 24px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="brand">LearnAI</div>
                        <div class="title">%s</div>
                        <div class="text">%s</div>
                        <div class="otp-box">
                            <div class="otp-code">%s</div>
                        </div>
                        <div class="text">Code expires in <strong>%d minutes</strong>.</div>
                        <div class="footer">
                            If you didn't create a LearnAI account or request a password reset, you can safely ignore this email.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(heading, messageBody, otpCode, expiryMinutes);
    }
}
