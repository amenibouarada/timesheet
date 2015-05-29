package com.aplana.timesheet.service.MailSenders;

/**
 * @author eshangareev
 * @version 1.0
 */
public class MailSenderTest {
    // ToDo to eshangareev: надо что-то сделать с тестами, после того как я поменял ccAddress
/*

    private static final String RIGHT_EMAIL = "ivanov@it.ru";
    private static final String RIGHT_MULTIPLE_EMAILS = "123@it.ru,234@it.ru,321@it.ru";
    private static final String SUBJECT = "Тема дня";

    @Test
    public void testGetMailSession() throws Exception {
        MailSender mailSender = new MailSender(null, null);
        Session mailSession = mailSender.getMailSession(new TSPropertyProvider());

        Assert.assertNotNull(Iterables.tryFind(Arrays.asList(mailSession.getProviders()), new Predicate<Provider>() {
            @Override
            public boolean apply(Provider input) {
                return input.getProtocol().equals("smtp");
            }
        }).orNull());

        Transport transport = mailSession.getTransport();
        Assert.assertNotNull(transport);
        transport.connect();
        Assert.assertTrue(transport.isConnected());
        transport.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitFromAddresses_WronEmailFormat() {
        MailSender mailSender = new MailSender(null, null);
        mailSender.initFromAddresses(getMail(true, false));
    }

    @Test
    public void testInitFromAddresses_RightEmailFormat() {
        MailSender mailSender = new MailSender(null, null);
        InternetAddress internetAddress = mailSender.initFromAddresses(getMail(false, false));
        Assert.assertEquals(internetAddress.getAddress(), RIGHT_EMAIL);
    }

    @Test
    public void testInitCcAddresses() {
        MailSender mailSender = new MailSender(null, null);
        Assert.assertNull(mailSender.initCcAddresses(getMail(true, false)));
        Assert.assertNotNull(mailSender.initCcAddresses(getMail(false, false)));
        Assert.assertEquals(mailSender.initCcAddresses(getMail(false, false))[0].getAddress(), RIGHT_EMAIL);
        Assert.assertEquals(mailSender.initCcAddresses(getMail(false, false)).length, 1);
        Assert.assertEquals(mailSender.initCcAddresses(getMail(false, true)).length, 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitToAddresses_WrongEmailFormat() {
        MailSender mailSender = new MailSender(null, null);
        mailSender.initToAddresses(getMail(true, false));
    }

    @Test
    public void testInitToAddresses() {
        MailSender mailSender = new MailSender(null, null);

        Assert.assertNotNull(mailSender.initToAddresses(getMail(false, false)));
        Assert.assertEquals(mailSender.initToAddresses(getMail(false, false))[0].getAddress(), RIGHT_EMAIL);
        Assert.assertEquals(mailSender.initToAddresses(getMail(false, false)).length, 1);
        Assert.assertEquals(mailSender.initToAddresses(getMail(false, true)).length, 3);

        Mail mail = getMail(false, false);
        List<String> toEmails = Arrays.asList("aaa@it.ru", "bbb@it.ru", "ccc@it.ru", "ddd@it.ru");
        mail.setToEmails(toEmails);
        InternetAddress[] internetAddresses = mailSender.initToAddresses(mail);
        Assert.assertEquals(internetAddresses.length, 4);

        Iterable<String> transform = Iterables.transform(Arrays.asList(internetAddresses),
                new Function<InternetAddress, String>() {
                    @Nullable @Override
                    public String apply(@Nullable InternetAddress input) {
                        return input.getAddress();
                    }
                });
        for (String toEmail : toEmails) {
            Assert.assertTrue(Iterables.contains(transform, toEmail));
        }
    }

    @Test
    public void testInitMessageSubject() throws MessagingException {
        MimeMessage mock = Mockito.mock(MimeMessage.class);

        MailSender mailSender = new MailSender(null, null);
        mailSender.initMessageSubject(getMail(false, false), mock);

        Mockito.verify(mock, Mockito.times(1)).setSubject(SUBJECT, "UTF-8");
    }

    private Mail getMail(boolean wrongParams, boolean multipleEmails) {

        Mail mail = new Mail();

        String wrongEmail = "123@@";
        mail.setFromEmail(wrongParams ? wrongEmail : RIGHT_EMAIL);
        mail.setCcEmail(wrongParams ? "   \n\t\n" :
                (multipleEmails ? RIGHT_MULTIPLE_EMAILS : RIGHT_EMAIL));
        mail.setToEmails(wrongParams? Arrays.asList(wrongEmail) : Arrays.asList(multipleEmails? RIGHT_MULTIPLE_EMAILS: RIGHT_EMAIL ));
        mail.setFromEmail(wrongParams? wrongEmail : RIGHT_EMAIL);
        mail.setSubject(SUBJECT);
        return mail;
    }
*/
}
