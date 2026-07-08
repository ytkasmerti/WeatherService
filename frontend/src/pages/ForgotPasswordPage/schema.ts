import * as yup from 'yup';

export const forgotPasswordSchema = yup.object({
    email: yup.string()
        .email('Некорректный email')
        .required('Введите свой email'),

    confirm: yup.string()
        .oneOf([yup.ref('password')], 'Пароли не совпадают')
        .required('Подтвердите пароль'),

    password: yup.string()
        .min(6, 'Пароль должен быть не менее 6 символов')
        .max(50, 'Пароль не может быть больше 50 символов')
        .required('Введите новый пароль'),

    code: yup.string()
        .required('Введите код подтверждения')
        .matches(/^[0-9]{6}$/, 'Код должен состоять ровно из 6 цифр'),
});