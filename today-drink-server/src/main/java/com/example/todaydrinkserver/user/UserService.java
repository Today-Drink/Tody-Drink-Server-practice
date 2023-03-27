package com.example.todaydrinkserver.user;

import com.example.todaydrinkserver.shop.ShopRepository;
import com.example.todaydrinkserver.shop.Shop;
import com.example.todaydrinkserver.shop.ShopDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final FavoriteShopRepository favoriteShopRepository;
    @Autowired
    private final ShopRepository shopRepository;
    @Transactional
    public UserDto getUser(Long userSn){
        Optional<User> user = userRepository.findById(userSn);
        List<FavoriteShop> favoriteShops = favoriteShopRepository.findAllByUser(user.get());
        List<ShopDto> shopList = new ArrayList<>();

        for(FavoriteShop f_shop : favoriteShops){
            Optional<Shop> shop = shopRepository.findById(f_shop.getId());
            ShopDto shopDto = ShopDto.builder()
                    .name(shop.get().getName())
                    .classify(shop.get().getClassify())
                    .num(shop.get().getNum())
                    .endTime(shop.get().getEndTime())
                    .address(shop.get().getAddress())
                    .latitude(shop.get().getLatitude())
                    .longitude(shop.get().getLongitude())
                    .build();
            shopList.add(shopDto);
        }

        return UserDto.builder()
                .userName(user.get().getUserName())
                .userId(user.get().getUserId())
                .userPwd((user.get().getUserPwd()))
                .favoriteShops(shopList)
                .build();
    }

    public String registerFavoriteShop(String userId, String shopName){
        Optional<User> user = userRepository.findByUserId(userId);
        Optional<Shop> shop = shopRepository.findByName(shopName);
        FavoriteShop user_shop = FavoriteShop.builder()
                .user(user.get())
                .shop(shop.get())
                .build();
        favoriteShopRepository.save(user_shop);
        return "register favorite shop success";
    }

    @Transactional
    public String registerUser(UserDto userDto){
        User user = User.builder()
                .userId(userDto.getUserId())
                .userName(userDto.getUserName())
                .userPwd(userDto.getUserPwd())
                .build();
        userRepository.save(user);
        return "save User";
    }

    public String login(UserDto userDto){
        log.info("user id = {}", userDto.getUserId());
        User member = userRepository.findByUserId(userDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));

        return jwtTokenProvider.createToken(member.getUsername(), member.getRoles());
    }

    public boolean logout(UserDto userDto){
       return true;
    }

}