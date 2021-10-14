package at.ac.tuwien.caa.docscan.logic

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import at.ac.tuwien.caa.docscan.DocScanApp
import at.ac.tuwien.caa.docscan.R
import at.ac.tuwien.caa.docscan.db.model.Page
import at.ac.tuwien.caa.docscan.db.model.exif.Rotation
import at.ac.tuwien.caa.docscan.gallery.CropRectTransformNew
import at.ac.tuwien.caa.docscan.glidemodule.GlideApp
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.MediaStoreSignature
import org.koin.java.KoinJavaComponent.inject
import timber.log.Timber
import java.io.File

/**
 * A helper utility class for glide.
 */
object GlideHelper {

    private val fileHandler: FileHandler by inject(FileHandler::class.java)
    private val app: DocScanApp by inject(DocScanApp::class.java)

    fun loadFileIntoImageView(
        file: File,
        rotation: Rotation,
        imageView: ImageView,
        style: GlideStyles,
        onResourceReady: (isFirstResource: Boolean) -> Unit = {},
        onResourceFailed: (isFirstResource: Boolean, e: GlideException?) -> Unit = { _, _ -> }
    ) {
        loadIntoView(
            app,
            imageView,
            null,
            file,
            FileType.JPEG,
            rotation,
            style,
            onResourceReady,
            onResourceFailed
        )
    }

    fun loadPageIntoImageView(
        page: Page?,
        imageView: ImageView,
        style: GlideStyles
    ) {
        loadPageIntoImageView(page, imageView, style, {}, { _, _ -> })
    }

    private fun loadPageIntoImageView(
        page: Page?,
        imageView: ImageView,
        style: GlideStyles,
        onResourceReady: (isFirstResource: Boolean) -> Unit = {},
        onResourceFailed: (isFirstResource: Boolean, e: GlideException?) -> Unit = { _, _ -> }
    ) {
        if (page != null) {
            val file = fileHandler.getFileByPage(page)
            if (file != null) {
                loadIntoView(
                    app,
                    imageView,
                    CropRectTransformNew(page, imageView.context),
                    file,
                    FileType.JPEG,
                    page.rotation,
                    style,
                    onResourceReady,
                    onResourceFailed
                )
                return
            }
            onResourceFailed.invoke(false, null)
        }

        Timber.w("Image file doesn't exist and cannot be shown with Glide!")
        // clear the image view in case that the file or even the page doesn't exist
        GlideApp.with(app).clear(imageView)
    }

    private fun loadIntoView(
        context: Context,
        imageView: ImageView,
        transformation: BitmapTransformation?,
        file: File,
        @Suppress("SameParameterValue") fileType: FileType,
        rotation: Rotation,
        style: GlideStyles,
        onResourceReady: (isFirstResource: Boolean) -> Unit = {},
        onResourceFailed: (isFirstResource: Boolean, e: GlideException?) -> Unit = { _, _ -> }
    ) {
        // TODO: add a cross fade as default, looks quite nice
        val glideRequest = GlideApp.with(context)
            .load(file)
            .signature(
                MediaStoreSignature(
                    fileType.mimeType,
                    file.lastModified(),
                    rotation.exifOrientation
                )
            ).listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    onResourceFailed(isFirstResource, e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    onResourceReady(isFirstResource)
                    return false
                }
            })

        val glideTransformRequest = when (style) {
            GlideStyles.DEFAULT -> {
                glideRequest
            }
            GlideStyles.CAMERA_THUMBNAIL -> {
                glideRequest.transform(CircleCrop())
            }
            GlideStyles.DOCUMENT_PREVIEW -> {
                glideRequest.transform(
                    CenterCrop(),
                    RoundedCorners(context.resources.getDimensionPixelSize(R.dimen.document_preview_corner_radius))
                )
            }
            GlideStyles.IMAGE_CROPPED -> {
                glideRequest
            }
            GlideStyles.IMAGES_UNCROPPED -> {
                transformation?.let {
                    glideRequest.transform(it)
                } ?: glideRequest
//                    TODO: check if this is necessary: .override(400, 400)
            }
        }

        glideTransformRequest.into(imageView)
    }

    enum class GlideStyles {
        DEFAULT,
        CAMERA_THUMBNAIL,
        DOCUMENT_PREVIEW,
        IMAGE_CROPPED,
        IMAGES_UNCROPPED
    }
}