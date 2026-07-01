import React from 'react';
import styles from './Pagination.module.css';

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

export const Pagination: React.FC<PaginationProps> = ({currentPage, totalPages, onPageChange}) => {
    const renderPages = () => {
        let pages = [];
        for (let i = 0; i < totalPages; i++) {
            // Логика отображения: первая, последняя, текущая и +/- 1 страница
            if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
                pages.push(
                    <button
                        key={i}
                        className={`${styles.pButton} ${currentPage === i ? styles.active : ''}`}
                        onClick={() => onPageChange(i)}
                    >
                        {i + 1}
                    </button>
                );
            } else if (i === currentPage - 2 || i === currentPage + 2) {
                pages.push(<span key={i} style={{padding: '0 8px', color: '#585f6a'}}>...</span>);
            }
        }
        return pages;
    };

    return (
        <div className={styles.paginationWrapper}>
            <button
                className={styles.pButton}
                onClick={() => onPageChange(Math.max(0, currentPage - 1))}
            >
                &lt;
            </button>

            {renderPages()}

            <button
                className={styles.pButton}
                onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
            >
                &gt;
            </button>
        </div>
    );
};