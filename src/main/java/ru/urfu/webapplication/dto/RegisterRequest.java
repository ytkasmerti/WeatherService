// Создать файл ru.urfu.webapplication.dto.RegisterRequest.java

package ru.urfu.webapplication.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String repeatPassword;
}