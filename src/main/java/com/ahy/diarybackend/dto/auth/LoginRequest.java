package com.ahy.diarybackend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 로그인 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

    @Schema(description = "회원 ID", example = "testuser")
    @NotBlank(message = "아이디는 필수입니다.")
    private String username;

    @Schema(description = "비밀번호", example = "qwer1234")
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

}
