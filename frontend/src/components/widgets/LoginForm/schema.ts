import * as yup from 'yup';

export const loginSchema = yup.object({
    email: yup.string()
        .email('Некорректный email')
        .required('Введите Email'),

    password: yup.string()
        .min(6, 'Пароль не может быть меньше 6 символов')
        .max(50, 'Пароль не может быть больше 50 символов')
        .required('Пароль обязателен')
});