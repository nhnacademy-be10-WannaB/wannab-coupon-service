package shop.wannab.couponservice.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gateway", url = "${gateway.api.url}", path = "/user-service", contextId = "userClient")
public interface UserServiceClient
{
    @GetMapping("/api/users/birthdays")
    List<Long> getBirthdayUserIds(@RequestParam int month);
}
