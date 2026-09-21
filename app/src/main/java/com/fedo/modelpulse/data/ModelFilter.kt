package com.fedo.modelpulse.data

/**
 * One entry of the provider filter. [slugs] is every provider id behind the
 * label: OpenRouter ships "meta/…" and "meta-llama/…", both displayed "Meta",
 * and two identical chips would read as a bug. [key] is the stable id the
 * selection is stored as — a display name would move when a provider renames
 * itself.
 */
data class ProviderFilter(
    val slugs: Set<String>,
    val name: String,
    val count: Int,
) {
    val key: String get() = slugs.min()
}

/**
 * Providers present in this list, biggest first. Grouped by [AiModel.providerSlug]
 * so a provider that renames itself stays one entry, and every model of a slug
 * merges into that single entry.
 */
fun List<AiModel>.providerFilters(): List<ProviderFilter> =
    groupBy(AiModel::providerName)
        .map { (name, models) ->
            ProviderFilter(
                slugs = models.mapTo(sortedSetOf(), AiModel::providerSlug),
                name = name,
                count = models.size,
            )
        }
        .sortedWith(compareByDescending(ProviderFilter::count).thenBy(ProviderFilter::name))

/**
 * Models matching both the query (name or id, case-insensitive) and the
 * provider entry. A blank query or a null provider matches everything.
 */
fun List<AiModel>.filterBy(query: String, providerSlugs: Set<String>?): List<AiModel> {
    val trimmed = query.trim()
    return filter { model ->
        (providerSlugs == null || model.providerSlug in providerSlugs) &&
            (
                trimmed.isEmpty() ||
                    model.shortName.contains(trimmed, ignoreCase = true) ||
                    model.id.contains(trimmed, ignoreCase = true)
                )
    }
}
