package at.ac.tuwien.caa.docscan.ui.docviewer.documents.selector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.ac.tuwien.caa.docscan.R
import at.ac.tuwien.caa.docscan.databinding.SelectDocumentRowLayoutBinding
import at.ac.tuwien.caa.docscan.db.model.DocumentWithPages
import at.ac.tuwien.caa.docscan.logic.GlideHelper

class SelectDocumentAdapter(private val clickListener: (DocumentWithPages) -> Unit) : ListAdapter<DocumentWithPages, SelectDocumentAdapter.ViewHolder>(DocumentWithPagesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                SelectDocumentRowLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(val binding: SelectDocumentRowLayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(document: DocumentWithPages) {

            val isEmpty = document.pages.isEmpty()
            if (!isEmpty) {
                val page = document.pages[0]
                GlideHelper.loadPageIntoImageView(
                        page,
                        binding.documentThumbnailImageview,
                        GlideHelper.GlideStyles.DOCUMENT_PREVIEW
                )
                binding.documentThumbnailImageview.transitionName = page.id.toString()
            } else {
                binding.documentThumbnailImageview.setImageResource(R.drawable.ic_do_not_disturb_black_24dp)
                binding.documentThumbnailImageview.setBackgroundColor(itemView.resources.getColor(R.color.second_light_gray))
            }

            binding.documentTitleText.text = document.document.title
            itemView.setOnClickListener { clickListener(document) }


            // TODO: Add a plural strings file for this case
//            1 image or  many images?
            val docDesc =
                    if (document.pages.size == 1) itemView.context.getText(R.string.sync_doc_image)
                    else itemView.context.getText(R.string.sync_doc_images)

            var desc = "${document.pages.size} $docDesc"
            binding.documentDescriptionTextview.text = "$desc"

        }
    }
}

// TODO: Check the necessary updates
class DocumentWithPagesDiffCallback : DiffUtil.ItemCallback<DocumentWithPages>() {
    override fun areItemsTheSame(
            oldItem: DocumentWithPages,
            newItem: DocumentWithPages
    ) = oldItem == newItem

    override fun areContentsTheSame(
            oldItem: DocumentWithPages,
            newItem: DocumentWithPages
    ) = oldItem == newItem
}
