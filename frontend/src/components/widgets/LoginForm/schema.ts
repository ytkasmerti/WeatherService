import * as yup from 'yup';

export const loginSchema = yup.object({
    email: yup.string()
        .email('Некорректный email')
        .required('Введите email для входа'),

    password: yup.string()
        .min(6, 'Пароль не может быть менее 6 символов')
        .max(50, 'Пароль не может быть более 50 символов')
        .required('Введите свой пароль')
});