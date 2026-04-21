package com.swer313.projectstep1.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Mail configuration.

 * - When spring.mail.mock=true  → uses MockMailSender (logs to console, never throws).
 * - Otherwise               → uses real JavaMailSenderImpl configured from application.properties.
 */
@Slf4j
@Configuration
public class MailConfig {

    /**
     * Mock mail sender — used for local development and tests.
     * Activate by setting spring.mail.mock=true in application.properties.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.mail.mock", havingValue = "true")
    public JavaMailSender mockMailSender() {
        log.warn("⚠️  MockMailSender is active — emails will NOT be delivered.");
        return new JavaMailSender() {
            @Override public MimeMessage createMimeMessage() {
                return new JavaMailSenderImpl().createMimeMessage();
            }
            @Override public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                return new JavaMailSenderImpl().createMimeMessage(contentStream);
            }
            @Override public void send(MimeMessage mimeMessage) throws MailException {
                log.info("[MOCK EMAIL] Sending MimeMessage — suppressed in mock mode");
            }
            @Override public void send(MimeMessage... mimeMessages) throws MailException {
                log.info("[MOCK EMAIL] Sending {} MimeMessage(s) — suppressed", mimeMessages.length);
            }
            @Override public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
                log.info("[MOCK EMAIL] Sending via preparator — suppressed in mock mode");
            }
            @Override public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
                log.info("[MOCK EMAIL] Sending {} preparator(s) — suppressed", mimeMessagePreparators.length);
            }
            @Override public void send(SimpleMailMessage simpleMessage) throws MailException {
                log.info("[MOCK EMAIL] To: {} | Subject: {}",
                        simpleMessage.getTo(), simpleMessage.getSubject());
            }
            @Override public void send(SimpleMailMessage... simpleMessages) throws MailException {
                for (SimpleMailMessage m : simpleMessages) {
                    log.info("[MOCK EMAIL] To: {} | Subject: {}", m.getTo(), m.getSubject());
                }
            }
        };
    }

    /**
     * Real SMTP sender — used in production.
     * Configured via spring.mail.* properties.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.mail.mock", havingValue = "false", matchIfMissing = true)
    public JavaMailSender realMailSender(
            @org.springframework.beans.factory.annotation.Value("${spring.mail.host}") String host,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.port}") int port,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.username}") String username,
            @org.springframework.beans.factory.annotation.Value("${spring.mail.password}") String password) {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth",          "true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.debug",              "false");

        return sender;
    }
}
