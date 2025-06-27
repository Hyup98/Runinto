package com.runinto.auth.service;

import com.runinto.auth.domain.CustomUserDetails;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import com.runinto.user.domain.repository.UserH2Repository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserH2Repository userRepository;

    public CustomUserDetailsService(UserH2Repository userRepository) {

        this.userRepository = userRepository;
    }

    //todo 임시로직 -> 돌아가게만 만듦
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //지금 임시로직 -> 수정해얃함
        User userData;
        if(username.equals("test")) {
            //userData = userRepository.findById(1L).orElse(null);
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "1234";
            String encodedPassword = encoder.encode(rawPassword);
            userData = User.builder()
                    .name("테스트유저")
                    .imgUrl("https://example.com/profile.jpg")
                    .description("임시 계정입니다.")
                    .gender(Gender.MALE)
                    .age(25)
                    .build();
        }
        else {
            userData = null;
        }

        if (userData != null) {
            //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
            return new CustomUserDetails(userData);
        }

        return null;
    }
}