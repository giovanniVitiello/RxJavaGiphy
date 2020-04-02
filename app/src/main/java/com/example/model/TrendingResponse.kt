package com.example.model

data class TrendingResponse(
    val data: ArrayList<ResponseGif>
) {
    data class ResponseGif(
        val type: String,
        val id: String,
        val url: String,
        val slug: String,
        val title: String,
        val images: Images
    ) {
        data class Images(
            val original: Original,
            val preview_gif: Preview
        ) {
            data class Original(
                val frames: String,
                val hash: String,
                val height: String,
                val mp4: String,
                val mp4_size: String,
                val size: String,
                val url: String,
                val webp: String,
                val webp_size: String,
                val width: String
            )

            data class Preview(
                val height: String,
                val size: String,
                val url: String,
                val width: String
            )
        }

        fun toGif(): Gif {
            return Gif(
                id = id,
                original = Gif.Original(images.original.url),
                preview = Gif.Preview(images.preview_gif.url)
            )
        }
    }
}
