package com.aplana.timesheet.service.MailSenders;

import com.aplana.timesheet.service.*;
import com.aplana.timesheet.system.properties.TSPropertyProvider;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.*;

public class MailSender<T> {

    protected static final Logger logger = LoggerFactory.getLogger(MailSender.class);
    protected static final String MAIL_BODY = "mail_body";
    protected static final int FIRST = 0;

    protected SendMailService sendMailService;
    protected TSPropertyProvider propertyProvider;
    protected VacationApprovalService vacationApprovalService;
    protected ManagerRoleNameService managerRoleNameService;
    protected OvertimeCauseService overtimeCauseService;

    public MailSender(SendMailService sendMailService, TSPropertyProvider propertyProvider) {
        this.sendMailService = sendMailService;
        this.propertyProvider = propertyProvider;
    }

    public MailSender(SendMailService sendMailService, TSPropertyProvider propertyProvider, OvertimeCauseService overtimeCauseService) {
        this.sendMailService = sendMailService;
        this.propertyProvider = propertyProvider;
        this.overtimeCauseService = overtimeCauseService;
    }

    public MailSender(SendMailService sendMailService, TSPropertyProvider propertyProvider,
                      VacationApprovalService vacationApprovalService, ManagerRoleNameService managerRoleNameService) {
        this.sendMailService = sendMailService;
        this.propertyProvider = propertyProvider;
        this.vacationApprovalService = vacationApprovalService;
        this.managerRoleNameService = managerRoleNameService;
    }

    public final void sendMessage(T params) {
        try {
            initAndSendMessage(getMailList(params));
        } catch (NoSuchProviderException e) {
            logger.error("Provider for {} protocol not found.", propertyProvider.getMailTransportProtocol(), e);
        } catch (MessagingException e) {
            logger.error("Error while sending email message.", e);
        }
    }

    protected void initAndSendMessage(List<Mail> mailList) throws MessagingException {
        Transport transport = null;
        try {
            Session session = getMailSession(propertyProvider);

            transport = session.getTransport();
            transport.connect();

            for (Mail mail : mailList) {
                MimeMessage message = new MimeMessage(session);

                initMessageHead(mail, message);

                if (ArrayUtils.isEmpty(message.getAllRecipients())) {
                    logger.info("No recipients in mail. Skipping...");
                    continue;
                }

                initMessageBody(mail, message);
                debugSendToMail(message);
                logger.info("Sending message.");
                if (Boolean.parseBoolean(propertyProvider.getMailSendEnable())) {
                    transport.sendMessage(message, message.getAllRecipients());
                    logger.info("Message sended.");
                } else {
                    try {
                        logger.info("Message is formed, but the sending off in the options. Message text: " + message.getContent().toString());
                        String mailDebugAddress = propertyProvider.getMailDebugAddress();
                        if (StringUtils.isNotBlank(mailDebugAddress)){
                            addDebugInfoAndChangeReceiver(message, mailDebugAddress);
                            transport.sendMessage(message, message.getAllRecipients());
                            logger.info("Message sended on debug address: " + mailDebugAddress);
                        }
                    } catch (IOException e) {
                        logger.debug("Sending error", e);
                    }
                }
            }
        } finally {
            try {
                if (transport != null) {
                    transport.close();
                }
            } catch (MessagingException e) {
                logger.error("Error while closing transport.", e);
            }
        }
    }

    private void debugSendToMail(Message message) throws MessagingException {
        logger.info("Mail will be sent from: {}", Arrays.toString(message.getFrom()));
        logger.info("Mail will be sent to: {}", Arrays.toString(message.getRecipients(MimeMessage.RecipientType.TO)));
        logger.info("Copy mail will be sent to: {}", message.getRecipients(MimeMessage.RecipientType.CC) != null ?
                Arrays.toString(message.getRecipients(MimeMessage.RecipientType.CC)) :
                "");
    }

    private String getDebugInfo(Message message){
        StringBuilder additionalMessageBody = new StringBuilder();
        try{
            additionalMessageBody.append("DEBUG INFORMATION:");
            additionalMessageBody.append("<br>Email from: " + Arrays.toString(message.getFrom()));
            additionalMessageBody.append("<br>Email to: " + Arrays.toString(message.getRecipients(MimeMessage.RecipientType.TO)));
            additionalMessageBody.append("<br>Email cc: ");
            additionalMessageBody.append(
                    message.getRecipients(MimeMessage.RecipientType.CC) != null ?
                    Arrays.toString(message.getRecipients(MimeMessage.RecipientType.CC)) :
                    ""
            );
            additionalMessageBody.append("<br>END DEBUG INFORMATION<br><br>");
        }catch(MessagingException ex){
            logger.error("Error while init debug info:", ex);
        }
        return additionalMessageBody.toString();
    }

    private void addDebugInfoAndChangeReceiver(MimeMessage message, String mailDebugAddress){
        try{
            String debugInfo = getDebugInfo(message);
            String charset = "UTF-8";
            message.setSubject("[TSDEBUG] " + message.getSubject(), charset);
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(mailDebugAddress));
            message.setRecipients(MimeMessage.RecipientType.CC, "");
            if (message.getContent() instanceof MimeMultipart){   // если это сложное письмо (напр, с вл. файлами)
                final Multipart multipart = (Multipart) message.getContent();
                final StringBuilder builder = new StringBuilder(debugInfo + multipart.getBodyPart(0).getDataHandler().getContent())
                        .append("<br>");

                for (int i = 1; i < multipart.getCount(); i++) {
                    final BodyPart bodyPart = multipart.getBodyPart(i);

                    if (StringUtils.isNotBlank(bodyPart.getFileName())) {
                        builder.append("<br>Присоединенный файл: ").append(bodyPart.getFileName());
                    }
                }

                message.setText(builder.toString(), charset, "html");
            } else{                                               // обычный текст
                message.setText(debugInfo + message.getContent(), charset, "html");
            }
        }catch (MessagingException ex){
            logger.error("Error while init message recipients.", ex);
        }catch (IOException ex){
            logger.error("Error get message content.", ex);
        }
    }

    private void initMessageHead(Mail mail, MimeMessage message) {
        InternetAddress fromAddr = initFromAddresses(mail);
        InternetAddress[] ccAddresses = initAddresses(mail.getCcEmails());
        InternetAddress[] toAddresses = initAddresses(mail.getToEmails());
        InternetAddress[] ccAssistantAddresses = initAddresses(getAssistantMail(mail.getToEmails()));
        InternetAddress[] ccAddressesWithAssistant;
        if (ccAddresses != null && ccAssistantAddresses != null) {
            ccAddressesWithAssistant = new InternetAddress[ccAddresses.length+ccAssistantAddresses.length];
            System.arraycopy(ccAddresses,0,ccAddressesWithAssistant,0,ccAddresses.length);
            System.arraycopy(ccAssistantAddresses,0,ccAddressesWithAssistant,ccAddresses.length,ccAssistantAddresses.length);

        } else if(ccAddresses != null){
            ccAddressesWithAssistant = ccAddresses;
        } else {
            ccAddressesWithAssistant = ccAssistantAddresses;
        }
        logger.debug("CC AssistantAddresses: {}", Arrays.toString(ccAssistantAddresses));
        logger.debug("CC AddressesWithAssistant: {}", Arrays.toString(ccAddressesWithAssistant));
        logger.debug("CC Addresses: {}", Arrays.toString(ccAddresses));
        logger.debug("TO Addresses: {}", Arrays.toString(toAddresses));

        try {
            message.setHeader("X-Priority", String.valueOf(mail.getPriority().getId()));
        } catch (MessagingException e) {
            logger.error("Error while set priority for message.", e);
        }

        try {
            initMessageSubject(mail, message);

            message.setRecipients(MimeMessage.RecipientType.TO, toAddresses);
            if (ccAddresses != null) {
                message.setRecipients(MimeMessage.RecipientType.CC, ccAddressesWithAssistant);
            }
            message.setFrom(fromAddr);
        } catch (MessagingException e) {
            logger.error("Error while init message recipients.", e);
         }
    }

    private Iterable<String> getAssistantMail(Iterable<String> toEmails) {
        Set<String> ccMailAss = new HashSet<String>();
        for(String x : toEmails){
            ccMailAss.add(x);
        }
        Iterable<String> ccMailAssistant = new SenderWithAssistants(sendMailService,propertyProvider).getAssistantEmail(ccMailAss);
        return ccMailAssistant;
    }

    @VisibleForTesting
    InternetAddress[] initAddresses(Iterable<String> emails){
        try {
            if (emails == null) {return null;}
            InternetAddress[] toAddresses = InternetAddress.parse(Joiner.on(",").join(emails));
            logger.debug("Email Address = {}", toAddresses);
            return toAddresses;
        } catch (AddressException e) {
            throw new IllegalArgumentException("Email address has wrong format.", e);
        }
    }

    @VisibleForTesting
    InternetAddress initFromAddresses(Mail mail) {
        String fromEmail = mail.getFromEmail();
        logger.debug("From Address = {}", fromEmail);
        try {
            return new InternetAddress(fromEmail);
        } catch (MessagingException e) {
            throw new IllegalArgumentException(String.format("Email address %s has wrong format.", fromEmail), e);
        }
    }

    @VisibleForTesting
    void initMessageSubject(Mail mail, MimeMessage message) throws MessagingException {
        String messageSubject = String.format(getSubjectFormat(), mail.getSubject());
        logger.debug("Message subject: {}", messageSubject);
        message.setSubject(messageSubject, "utf-8");
    }

    /**
     * В формате допустимо использовать только один аргумент и только %s
     * @return
     */
    protected String getSubjectFormat() {
        return "%s";
    }

    @Autowired
    ReportService reportService;

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void initMessageBody(Mail mail, MimeMessage message) throws MessagingException {
        message.setText(mail.getPreconstructedMessageBody(), "UTF-8", "html");
    }

    protected List<Mail> getMailList(T params) {
        throw new IllegalAccessError("You must to ovverid getMailList method!");
    }

    @VisibleForTesting
    Session getMailSession(TSPropertyProvider propertyProvider) {
        Properties sysProperties = TSPropertyProvider.getProperties();
        if (StringUtils.isBlank(propertyProvider.getMailSmtpPort())) {
            sysProperties.put("mail.smtp.port", "25");
        }
        Session session;
        if (Boolean.parseBoolean(propertyProvider.getMailSmtpAuth())) {
            session = Session.getInstance(sysProperties, new SMTPAuthenticator());
        } else {
            session = Session.getInstance(sysProperties);
        }
        session.setDebug(true);
        return session;
    }

    private class SMTPAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(propertyProvider.getMailUsername(), propertyProvider.getMailPassword());
        }
    }
}