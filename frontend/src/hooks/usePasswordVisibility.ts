import {useState} from 'react';

export const usePasswordVisibility = () => {
    const [show, setShow] = useState(false);
    const toggle = () => setShow(!show);
    return {show, toggle};
};