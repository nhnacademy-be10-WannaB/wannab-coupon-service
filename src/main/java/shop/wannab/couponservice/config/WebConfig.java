package shop.wannab.couponservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {


    //임시 향후 삭제 예정
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/ 로 시작하는 모든 경로에 대해
                .allowedOrigins("http://localhost:8080") // 프론트엔드 서버의 주소 (포트번호까지 정확히)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*")    // 허용할 헤더
                .allowCredentials(true); // 쿠키 등 자격 증명 허용 여부
    }
}
