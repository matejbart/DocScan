package at.ac.tuwien.caa.docscan.ui.docviewer.pdf

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.ac.tuwien.caa.docscan.R
import at.ac.tuwien.caa.docscan.databinding.LayoutPdflistRowBinding
import at.ac.tuwien.caa.docscan.extensions.DATE_FORMAT_EXPORT_FILE
import at.ac.tuwien.caa.docscan.extensions.asTimeStamp
import at.ac.tuwien.caa.docscan.extensions.sizeMB

class PdfAdapter(
    private val select: (ExportList) -> Unit,
    private val options: (ExportList) -> Unit
) : ListAdapter<ExportList, PdfAdapter.ViewHolder>(PDFAdapterDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(LayoutPdflistRowBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: LayoutPdflistRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(export: ExportList) {
            when (export) {
                is ExportList.ExportHeader -> {

                }
                is ExportList.File -> {
                    binding.layoutPdflistRowTitle.text = export.name
                    // TODO: Use the android's size formatter instead
                    binding.layoutPdflistRowFilesize.text = "${export.file.sizeInBytes.sizeMB()} MB"
                    binding.layoutPdflistRowDate.text =
                        export.file.lastModified.asTimeStamp(dateFormat = DATE_FORMAT_EXPORT_FILE)
                    val iconRes =
                        if (export.isNew) R.drawable.ic_pdf_icon_badge else R.drawable.ic_pdf_icon
                    binding.layoutPdflistRowIcon.setImageResource(iconRes)
                    binding.layoutPdflistMoreButton.setOnClickListener {
                        options(export)
                    }

                    itemView.setOnClickListener {
                        select.invoke(export)
                    }
                }
            }
        }
    }
}

class PDFAdapterDiff : DiffUtil.ItemCallback<ExportList>() {
    override fun areItemsTheSame(
        oldItem: ExportList,
        newItem: ExportList
    ): Boolean {
        val oldFile = oldItem as? ExportList.File
        val newFile = newItem as? ExportList.File
        return oldFile?.name == newFile?.name && oldFile?.file?.documentId == newFile?.file?.documentId
    }

    override fun areContentsTheSame(
        oldItem: ExportList,
        newItem: ExportList
    ): Boolean {
        val oldFile = oldItem as? ExportList.File
        val newFile = newItem as? ExportList.File
        return oldFile?.name == newFile?.name && oldFile?.pageFileType == newFile?.pageFileType && oldFile?.isNew == newFile?.isNew && oldFile?.file?.lastModified == newFile?.file?.lastModified && oldFile?.file?.sizeInBytes == newFile?.file?.sizeInBytes
    }
}
