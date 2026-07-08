import * as yup from 'yup';

export const upgradeSchema = yup.object({
    cvc: yup.string()
        .matches(/^[0-9]{3}$/, 'CVC должен содержать 3 цифры'),
    expiry: yup.string()
        .matches(/^(0[1-9]|1[0-2])\/?([0-9]{2})$/, 'Введите срок в формате ММ/ГГ'),
    cardNumber: yup.string()
        .matches(/^[0-9]{16}$/, 'Номер карты должен состоять из 16 цифр'),
});