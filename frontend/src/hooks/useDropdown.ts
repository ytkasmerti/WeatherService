import {useState} from 'react';

export const useDropdown = (initialState = false) => {
    const [isOpen, setIsOpen] = useState(initialState);
    const toggle = () => setIsOpen(!isOpen);
    const close = () => setIsOpen(false);
    const arrow = isOpen ? '▲' : '▼';
    return {isOpen, toggle, close, arrow};
};