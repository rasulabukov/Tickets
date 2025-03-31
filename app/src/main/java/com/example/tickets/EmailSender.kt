import android.util.Log
import java.util.Properties
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender(
    private val username: String,
    private val password: String
) {
    fun sendEmail(recipient: String, subject: String, message: String): Boolean {
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.mail.ru")
            put("mail.smtp.port", "465")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.auth", "true")
        }

        val session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

        return try {
            val mimeMessage = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
                setSubject(subject)
                setText(message)
            }

            Transport.send(mimeMessage)
            true
        } catch (e: MessagingException) {
            Log.e("EmailSender", "Error sending email", e)
            false
        }
    }
}