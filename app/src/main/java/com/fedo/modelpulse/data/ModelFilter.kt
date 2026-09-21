package com.fedo.modelpulse.data

/** One entry of the provider filter: the stable key, its label and its size. */
data class ProviderFilter(
    val slug: String,
    val name: String,
    val count: Int,
)

/**
 * Providers present in this list, biggest first. Grouped by [AiModel.providerSlug]
 * so a provider that renames itself stays one entry, and every model of a slug
 * merges into that single entry.
 */
fun List<AiModel>.providerFilters(): List<ProviderFilter> =
    groupBy(AiModel::providerSlug)
        .map { (slug, models) -> ProviderFilter(slug, models.first().providerName, models.size) }
        .sortedWith(compareByDescending(ProviderFilter::count).thenBy(ProviderFilter::name))

/**
 * Models matching both the query (name or id, case-insensitive) and the
 * provider. A blank query or a null provider matches everything.
 */
fun List<AiModel>.filterBy(query: String, providerSlug: String?): List<AiModel> {
    val trimmed = query.trim()
    return filter { model ->
        (providerSlug == null || model.providerSlug == providerSlug) &&
            (
                trimmed.isEmpty() ||
                    model.shortName.contains(trimmed, ignoreCase = true) ||
                    model.id.contains(trimmed, ignoreCase = true)
                )
    }
}
