package com.project.lol.innertube.models.response

import com.project.lol.innertube.models.NavigationEndpoint
import com.project.lol.innertube.models.Runs
import kotlinx.serialization.Serializable

/**
 * Response of the `next` endpoint, trimmed to the tab list.
 *
 * We only need to locate the "Lyrics" tab and read its `browseId`. That id is an opaque
 * per-track entity id (e.g. `MPLYt_gJiD7tpHIQi-1`) and does **not** derive from the videoId,
 * so the `next` call is mandatory before any lyrics browse.
 */
@Serializable
data class NextResponse(
    val contents: Contents? = null,
) {
    @Serializable
    data class Contents(
        val singleColumnMusicWatchNextResultsRenderer: SingleColumn? = null,
    ) {
        @Serializable
        data class SingleColumn(
            val tabbedRenderer: Tabbed? = null,
        ) {
            @Serializable
            data class Tabbed(
                val watchNextTabbedResultsRenderer: WatchNext? = null,
            ) {
                @Serializable
                data class WatchNext(
                    val tabs: List<Tab>? = null,
                ) {
                    @Serializable
                    data class Tab(
                        val tabRenderer: TabRenderer? = null,
                    ) {
                        @Serializable
                        data class TabRenderer(
                            val title: String? = null,
                            val endpoint: NavigationEndpoint? = null,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Response of `browse` on a lyrics browseId.
 *
 * The client decides which branch is populated:
 *  - **WEB_REMIX** -> [SectionListRenderer] with plain text lyrics (no timing).
 *  - **ANDROID_MUSIC** -> [TimedLyricsModel] with per-line millisecond cues.
 *
 * Both are modelled here so a single response class covers the timed call and its fallback.
 */
@Serializable
data class LyricsResponse(
    val contents: Contents? = null,
) {
    @Serializable
    data class Contents(
        val elementRenderer: ElementRenderer? = null,
        val sectionListRenderer: SectionListRenderer? = null,
    ) {
        @Serializable
        data class ElementRenderer(
            val newElement: NewElement? = null,
        ) {
            @Serializable
            data class NewElement(
                val type: Type? = null,
            ) {
                @Serializable
                data class Type(
                    val componentType: ComponentType? = null,
                ) {
                    @Serializable
                    data class ComponentType(
                        val model: Model? = null,
                    ) {
                        @Serializable
                        data class Model(
                            val timedLyricsModel: TimedLyricsModel? = null,
                        ) {
                            @Serializable
                            data class TimedLyricsModel(
                                val lyricsData: LyricsData? = null,
                            )
                        }
                    }
                }
            }
        }

        @Serializable
        data class SectionListRenderer(
            val contents: List<Content>? = null,
        ) {
            @Serializable
            data class Content(
                val musicDescriptionShelfRenderer: MusicDescriptionShelfRenderer? = null,
            ) {
                @Serializable
                data class MusicDescriptionShelfRenderer(
                    val description: Runs? = null,
                    val footer: Runs? = null,
                )
            }
        }
    }
}

/**
 * Timed lyrics payload. `startTimeMilliseconds` / `endTimeMilliseconds` are strings in the
 * API, so they are parsed rather than typed as numbers.
 */
@Serializable
data class LyricsData(
    val timedLyricsData: List<TimedLine>? = null,
    val sourceMessage: String? = null,
) {
    @Serializable
    data class TimedLine(
        val lyricLine: String? = null,
        val cueRange: CueRange? = null,
    ) {
        @Serializable
        data class CueRange(
            val startTimeMilliseconds: String? = null,
            val endTimeMilliseconds: String? = null,
        )
    }
}
