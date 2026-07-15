import {getPaginationRange} from '../../../utils/pagination.ts';
import styles from './styles.module.css';

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
}

export const Pagination = ({currentPage, totalPages, onPageChange}: PaginationProps) => {
    const pages = getPaginationRange(currentPage, totalPages);

    return (
        <div className={styles.paginationWrapper}>
            <button className={styles.pButton} onClick={() => onPageChange(Math.max(0, currentPage - 1))}>&lt;</button>
            {pages.map((p) => (
                p.type === 'page' ? (
                    <button key={p.value}
                            className={`${styles.pButton} ${currentPage === p.value ? styles.active : ''}`}
                            onClick={() => onPageChange(p.value)}
                    >
                        {p.value + 1}
                    </button>
                ) : (
                    <span key={p.value} className={styles.ellipsis}>...</span>
                )
            ))}
            <button className={styles.pButton}
                    onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}>&gt;
            </button>
        </div>
    );
};