package org.dependencytrack.persistence;

import alpine.model.Team;
import com.github.packageurl.PackageURL;
import org.dependencytrack.model.Classifier;
import org.dependencytrack.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for filters meant to be used with {@link javax.jdo.Query#setFilter} and the query's
 * parameters that can be passed to {@link alpine.persistence.AbstractAlpineQueryManager#execute}
 * <br>
 * Mutable and not threadsafe!
 */
class ProjectQueryFilterBuilder {

    private final Map<String, Object> params;
    private final List<String> filterCriteria;

    ProjectQueryFilterBuilder() {
        this.params = new HashMap<>();
        this.filterCriteria = new ArrayList<>();
    }

    ProjectQueryFilterBuilder excludeInactive(boolean excludeInactive) {
        if (excludeInactive) {
            filterCriteria.add("(active == true || active == null)");
        }
        return this;
    }

    ProjectQueryFilterBuilder withTeam(Team team) {
        params.put("team", team);
        filterCriteria.add("(accessTeams.contains(:team))");
        return this;
    }

    ProjectQueryFilterBuilder withName(String name) {
        params.put("name", name);
        filterCriteria.add("(name == :name)");
        return this;
    }

    ProjectQueryFilterBuilder withVersion(String version) {
        params.put("version", version);
        filterCriteria.add("(version == :version)");
        return this;
    }

    ProjectQueryFilterBuilder withTag(Tag tag) {
        params.put("tag", tag);
        filterCriteria.add("(tags.contains(:tag))");
        return this;
    }

    ProjectQueryFilterBuilder withClassifier(Classifier classifier) {
        params.put("classifier", classifier);
        filterCriteria.add("(classifier == :classifier)");
        return this;
    }

    ProjectQueryFilterBuilder withFuzzyName(String name) {
        params.put("name", name);

        filterCriteria.add("(name.toLowerCase().matches(:name))");
        return this;
    }

    ProjectQueryFilterBuilder withFuzzyNameOrExactTag(String name, Tag tag) {
        params.put("name", name);
        params.put("tag", tag);

        filterCriteria.add("(name.toLowerCase().matches(:name) || tags.contains(:tag))");
        return this;
    }

    ProjectQueryFilterBuilder excludeChildProjects() {
        filterCriteria.add("parent == null");
        return this;
    }

    ProjectQueryFilterBuilder withParent(UUID uuid){
        params.put("parentUuid", uuid);

        filterCriteria.add("parent.uuid == :parentUuid");
        return this;
    }

    ProjectQueryFilterBuilder withPurlOrCpeOrSwid(PackageURL purl, String cpe, String swidTagId){
        StringBuilder filter = new StringBuilder();
        int terms=0;
        if (purl!=null) {
            params.put("purl", purl.canonicalize());
            filter.append("purl == :purl ||");
            terms++;
        }
        if (cpe!=null) {
            params.put("cpe", cpe);
            filter.append("cpe == :cpe ||");
            terms++;
        }
        if (swidTagId!=null) {
            params.put("swidTagId", swidTagId);
            filter.append("swidtagid == :swidTagId ||");
            terms++;
        }

        if (terms > 0) {
            filter.setLength(filter.length()-3);
            if (terms > 1) {
                filter.insert(0, "(");
                filter.append(")");
            }
            filterCriteria.add(filter.toString());
        }

        return this;
    }

    String buildFilter() {
        return String.join(" && ", this.filterCriteria);
    }

    Map<String, Object> getParams() {
        return params;
    }
}
