package io.github.javamodernizationlabs.jspecify.rewrite;

import io.github.javamodernizationlabs.jspecify.AnnotationCatalog;
import org.openrewrite.Recipe;
import org.openrewrite.java.ChangeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts well-known legacy nullness annotations to their JSpecify
 * counterparts via composed {@link ChangeType} recipes.
 *
 * <p>Per spec section 28.2, this recipe only performs <em>semantically
 * safe</em> 1:1 substitutions. Aliases, meta-annotations and package-level
 * defaults are intentionally left to manual review.
 */
public class ConvertKnownAnnotations extends Recipe {

    @Override
    public String getDisplayName() {
        return "Convert known nullness annotations to JSpecify";
    }

    @Override
    public String getDescription() {
        return "Rewrites imports and references of well-known legacy nullness annotations "
                + "(JetBrains, JSR-305, Spring, FindBugs, Checker Framework, etc.) to their "
                + "JSpecify counterparts. Ambiguous declaration-vs-type-use placements are "
                + "left unchanged and reported instead.";
    }

    @Override
    public List<Recipe> getRecipeList() {
        List<Recipe> recipes = new ArrayList<>();
        for (var mapping : AnnotationCatalog.defaults().mappings().entrySet()) {
            recipes.add(new ChangeType(mapping.getKey(), mapping.getValue(), false));
        }
        return recipes;
    }
}
