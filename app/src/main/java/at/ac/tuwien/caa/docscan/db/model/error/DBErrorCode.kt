package at.ac.tuwien.caa.docscan.db.model.error

enum class DBErrorCode {
    GENERIC,
    DOCUMENT_LOCKED,
    DOCUMENT_PARTIALLY_LOCKED,
    DOCUMENT_ALREADY_UPLOADED,
    DOCUMENT_NOT_CROPPED,
    DOCUMENT_DIFFERENT_UPLOAD_EXPECTATIONS,
    DOCUMENT_PAGE_FILE_FOR_UPLOAD_MISSING,
    ENTRY_NOT_AVAILABLE,
    DUPLICATE
}
