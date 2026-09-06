package com.enrichable.formatter;

import com.enrichable.model.EnrichInformation;
import java.util.List;
/**
 * Contract for formatting a list of {@link EnrichInformation} entries
 * into a human-readable string.
 *
 * <p>Implementations decide how the entries are structured, what fields
 * are included, and how the output is styled. The default implementation
 * is {@link DefaultEnrichFormatter}, which formats entries for console output
 * according to a {@link com.enrichable.config.ConsoleConfig}.</p>
 *
 * <p>Custom implementations can be used wherever a different output style
 * is needed — for example, a compact single-line format, a JSON format,
 * or an HTML report.</p>
 *
 * @see DefaultEnrichFormatter
 * @see EnrichInformation
 */
public interface EnrichFormatter {
    /**
     * <h5>Format a list of error entries into a string</h5>
     *
     * <p>The structure and content of the output are determined by the
     * implementation. The list passed in reflects the entries already
     * filtered and ordered by the caller.</p>
     *
     * @param informationList the error entries to format; must not be {@code null}
     * @return a formatted, human-readable string representing the entries
     */
    String format(List<EnrichInformation> informationList);
}