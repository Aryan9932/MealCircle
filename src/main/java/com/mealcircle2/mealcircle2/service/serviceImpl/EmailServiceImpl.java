package com.mealcircle2.mealcircle2.service.serviceImpl;

import com.mealcircle2.mealcircle2.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    // -------------------------------------------------------------------------
    // Welcome Email
    // -------------------------------------------------------------------------
    @Async
    @Override
    public void sendWelcomeEmail(String customerEmail, String messName, String joiningDate, String endingDate) {
        String subject = "Welcome to " + messName + " - Your Subscription is Confirmed!";
        String html = buildWelcomeHtml(customerEmail, messName, joiningDate, endingDate);
        sendHtmlEmail(customerEmail, subject, html);
    }

    // -------------------------------------------------------------------------
    // Absent Email
    // -------------------------------------------------------------------------
    @Async
    @Override
    public void sendAbsentEmail(String customerEmail, String messName, String date) {
        String subject = "Absence Recorded - " + messName + " (" + date + ")";
        String html = buildAbsentHtml(customerEmail, messName, date);
        sendHtmlEmail(customerEmail, subject, html);
    }

    // -------------------------------------------------------------------------
    // Present Email
    // -------------------------------------------------------------------------
    @Async
    @Override
    public void sendPresentEmail(String customerEmail, String messName, String date) {
        String subject = "Attendance Confirmed - " + messName + " (" + date + ")";
        String html = buildPresentHtml(customerEmail, messName, date);
        sendHtmlEmail(customerEmail, subject, html);
    }

    // -------------------------------------------------------------------------
    // Internal sender
    // -------------------------------------------------------------------------
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("[EmailService] Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // HTML Templates
    // -------------------------------------------------------------------------

    private String buildWelcomeHtml(String email, String messName, String joiningDate, String endingDate) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;font-family:Segoe UI,Arial,sans-serif;background:#f4f4f4;'>"
            + "<div style='max-width:600px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.10);'>"
            + "<div style='background:linear-gradient(135deg,#FF6B35 0%,#F7C59F 100%);padding:40px 32px;text-align:center;'>"
            + "<div style='font-size:48px;'>&#127859;</div>"
            + "<h1 style='color:#fff;margin:12px 0 4px;font-size:26px;font-weight:700;'>Welcome to MealCircle!</h1>"
            + "<p style='color:rgba(255,255,255,0.9);margin:0;font-size:15px;'>Your subscription is confirmed</p>"
            + "</div>"
            + "<div style='padding:36px 32px;'>"
            + "<p style='color:#333;font-size:16px;margin:0 0 24px;'>Hi <b>" + email + "</b>,</p>"
            + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 28px;'>You have successfully subscribed to <b style='color:#FF6B35;'>" + messName + "</b>. Get ready for delicious home-style meals every day!</p>"
            + "<div style='background:#FFF8F5;border:1.5px solid #FFE0CC;border-radius:12px;padding:24px;margin-bottom:28px;'>"
            + "<h3 style='color:#FF6B35;margin:0 0 16px;font-size:15px;text-transform:uppercase;letter-spacing:1px;'>Subscription Details</h3>"
            + "<table style='width:100%;border-collapse:collapse;'>"
            + "<tr><td style='color:#888;font-size:14px;padding:6px 0;width:40%;'>Mess Name</td><td style='color:#333;font-size:14px;font-weight:600;padding:6px 0;'>" + messName + "</td></tr>"
            + "<tr><td style='color:#888;font-size:14px;padding:6px 0;'>Start Date</td><td style='color:#333;font-size:14px;font-weight:600;padding:6px 0;'>" + joiningDate + "</td></tr>"
            + "<tr><td style='color:#888;font-size:14px;padding:6px 0;'>End Date</td><td style='color:#333;font-size:14px;font-weight:600;padding:6px 0;'>" + endingDate + "</td></tr>"
            + "</table>"
            + "</div>"
            + "<p style='color:#555;font-size:14px;line-height:1.7;'>You can mark yourself absent by 3:00 PM each day to save your buffer days. Log in to your MealCircle account to manage your subscription.</p>"
            + "</div>"
            + "<div style='background:#FFF8F5;padding:20px 32px;text-align:center;border-top:1px solid #FFE0CC;'>"
            + "<p style='color:#aaa;font-size:12px;margin:0;'>&#169; 2024 MealCircle. Eat well, every day.</p>"
            + "</div>"
            + "</div></body></html>";
    }

    private String buildAbsentHtml(String email, String messName, String date) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;font-family:Segoe UI,Arial,sans-serif;background:#f4f4f4;'>"
            + "<div style='max-width:600px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.10);'>"
            + "<div style='background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);padding:40px 32px;text-align:center;'>"
            + "<div style='font-size:48px;'>&#128197;</div>"
            + "<h1 style='color:#fff;margin:12px 0 4px;font-size:26px;font-weight:700;'>Absence Recorded</h1>"
            + "<p style='color:rgba(255,255,255,0.9);margin:0;font-size:15px;'>Your attendance has been updated</p>"
            + "</div>"
            + "<div style='padding:36px 32px;'>"
            + "<p style='color:#333;font-size:16px;margin:0 0 24px;'>Hi <b>" + email + "</b>,</p>"
            + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 28px;'>Your absence has been recorded at <b style='color:#764ba2;'>" + messName + "</b> for the date:</p>"
            + "<div style='background:#F5F3FF;border:1.5px solid #D1C4E9;border-radius:12px;padding:24px;margin-bottom:28px;text-align:center;'>"
            + "<div style='color:#764ba2;font-size:22px;font-weight:700;'>" + date + "</div>"
            + "<div style='color:#888;font-size:13px;margin-top:6px;'>Absence Date</div>"
            + "</div>"
            + "<div style='background:#FFF8E1;border-left:4px solid #FFC107;padding:16px 20px;border-radius:8px;margin-bottom:20px;'>"
            + "<p style='color:#795548;font-size:14px;margin:0;line-height:1.6;'><b>Note:</b> Your mess end date has been extended by 1 day and 1 buffer day has been used. You can remove this absence before 3:00 PM today if needed.</p>"
            + "</div>"
            + "</div>"
            + "<div style='background:#F5F3FF;padding:20px 32px;text-align:center;border-top:1px solid #D1C4E9;'>"
            + "<p style='color:#aaa;font-size:12px;margin:0;'>&#169; 2024 MealCircle. Eat well, every day.</p>"
            + "</div>"
            + "</div></body></html>";
    }

    private String buildPresentHtml(String email, String messName, String date) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;font-family:Segoe UI,Arial,sans-serif;background:#f4f4f4;'>"
            + "<div style='max-width:600px;margin:40px auto;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.10);'>"
            + "<div style='background:linear-gradient(135deg,#11998e 0%,#38ef7d 100%);padding:40px 32px;text-align:center;'>"
            + "<div style='font-size:48px;'>&#9989;</div>"
            + "<h1 style='color:#fff;margin:12px 0 4px;font-size:26px;font-weight:700;'>Attendance Confirmed</h1>"
            + "<p style='color:rgba(255,255,255,0.9);margin:0;font-size:15px;'>Enjoy your meal today!</p>"
            + "</div>"
            + "<div style='padding:36px 32px;'>"
            + "<p style='color:#333;font-size:16px;margin:0 0 24px;'>Hi <b>" + email + "</b>,</p>"
            + "<p style='color:#555;font-size:15px;line-height:1.7;margin:0 0 28px;'>Your presence has been confirmed at <b style='color:#11998e;'>" + messName + "</b> for the date:</p>"
            + "<div style='background:#F0FFF4;border:1.5px solid #C6F6D5;border-radius:12px;padding:24px;margin-bottom:28px;text-align:center;'>"
            + "<div style='color:#11998e;font-size:22px;font-weight:700;'>" + date + "</div>"
            + "<div style='color:#888;font-size:13px;margin-top:6px;'>Attendance Date</div>"
            + "</div>"
            + "<div style='background:#F0FFF4;border-left:4px solid #38ef7d;padding:16px 20px;border-radius:8px;margin-bottom:20px;'>"
            + "<p style='color:#276749;font-size:14px;margin:0;line-height:1.6;'><b>Great!</b> Your presence has been noted. Bon appetit - enjoy your delicious meal at " + messName + "!</p>"
            + "</div>"
            + "</div>"
            + "<div style='background:#F0FFF4;padding:20px 32px;text-align:center;border-top:1px solid #C6F6D5;'>"
            + "<p style='color:#aaa;font-size:12px;margin:0;'>&#169; 2024 MealCircle. Eat well, every day.</p>"
            + "</div>"
            + "</div></body></html>";
    }
}