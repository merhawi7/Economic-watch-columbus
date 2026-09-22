/**
 * Sends alerts when the risk category changes. This is a stub implementation
 * — it prints what it WOULD send. To make this real:
 *   - Sound: use java.awt.Toolkit.getDefaultToolkit().beep() or a sound clip
 *   - Email: wire in JavaMail (SMTP) with real credentials
 *   - SMS: wire in a provider API (e.g. Twilio) with a real account
 */
public class AlertService {

    private boolean soundEnabled = true;
    private boolean emailEnabled = true;
    private boolean smsEnabled = true;
    private String emailAddress = "merafere@gmail.com";

    public void setSoundEnabled(boolean v) { this.soundEnabled = v; }
    public void setEmailEnabled(boolean v) { this.emailEnabled = v; }
    public void setSmsEnabled(boolean v) { this.smsEnabled = v; }
    public void setEmailAddress(String v) { this.emailAddress = v; }

    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public boolean isSmsEnabled() { return smsEnabled; }
    public String getEmailAddress() { return emailAddress; }

    public void notifyRiskChange(RiskScoreCalculator.RiskCategory oldCategory,
                                  RiskScoreCalculator.RiskCategory newCategory) {
        if (oldCategory == newCategory) return;

        String message = String.format("Risk level changed: %s -> %s %s",
                oldCategory.label, newCategory.icon, newCategory.label);

        if (soundEnabled) {
            System.out.println("[ALERT:SOUND] " + message + " (would play an alert tone)");
        }
        if (emailEnabled) {
            System.out.println("[ALERT:EMAIL] Would send to " + emailAddress + ": " + message);
        }
        if (smsEnabled) {
            System.out.println("[ALERT:SMS] Would send SMS: " + message);
        }
    }
}
