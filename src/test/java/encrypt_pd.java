import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class encrypt_pd {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin1234567"));

    }
}
//admin1密码:
// admin123456
// $2a$10$ERaRboRc0JVOC8vmpMlQFeRIszXNX9ptCfXuuWATPCR581eBsnvtC

//admin2密码:
// admin1234567
// $2a$10$.69czYvPv6GkZg/Zxbj2zuQAqgZYT5Yc3G7esnq9iwJ4iIXQJEGyq