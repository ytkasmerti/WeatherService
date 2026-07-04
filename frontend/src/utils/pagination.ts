export const getPaginationRange = (currentPage: number, totalPages: number) => {
    let pages = [];
    for (let i = 0; i < totalPages; i++) {
        if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
            pages.push({type: 'page', value: i});
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            pages.push({type: 'ellipsis', value: i});
        }
    }
    return pages;
};