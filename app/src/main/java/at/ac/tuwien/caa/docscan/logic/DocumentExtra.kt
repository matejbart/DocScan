package at.ac.tuwien.caa.docscan.logic

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class DocumentPage(
        val docId: UUID,
        val pageId: UUID
) : Parcelable
