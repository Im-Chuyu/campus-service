import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class encrypt_pd {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode(args.length > 0 ? args[0] : "change-me"));
    }
}
