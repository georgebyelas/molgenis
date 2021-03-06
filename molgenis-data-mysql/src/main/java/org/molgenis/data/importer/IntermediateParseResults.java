package org.molgenis.data.importer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EditableEntityMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Tag;
import org.molgenis.data.semantic.TagImpl;
import org.molgenis.data.support.DefaultEntityMetaData;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Mutable bean to store intermediate parse results. Uses lookup tables to map simple names to the parsed objects. Is
 * used by the {@link EmxMetaDataParser}
 */
public final class IntermediateParseResults
{
	/**
	 * Maps simple name to EntityMetaData
	 */
	private final Map<String, DefaultEntityMetaData> entities;
	/**
	 * Maps simple name to PackageImpl (with tags)
	 */
	private final Map<String, PackageImpl> packages;
	/**
	 * Contains all Attribute tags
	 */
	private final SetMultimap<String, Tag<AttributeMetaData, LabeledResource, LabeledResource>> attributeTags;
	/**
	 * Contains all Entity tags
	 */
	private final List<Tag<EntityMetaData, LabeledResource, LabeledResource>> entityTags;
	/**
	 * Contains all tag entities from the tag sheet
	 */
	private final Map<String, Entity> tags;

	public IntermediateParseResults()
	{
		this.tags = new LinkedHashMap<String, Entity>();
		this.entities = new LinkedHashMap<String, DefaultEntityMetaData>();
		this.packages = new LinkedHashMap<String, PackageImpl>();
		this.attributeTags = LinkedHashMultimap
				.<String, Tag<AttributeMetaData, LabeledResource, LabeledResource>> create();
		this.entityTags = new ArrayList<Tag<EntityMetaData, LabeledResource, LabeledResource>>();
	}

	public void addTagEntity(String identifier, Entity tagEntity)
	{
		tags.put(identifier, tagEntity);
	}

	public void addAttributes(String simpleEntityName, List<AttributeMetaData> editableEntityMetaData)
	{
		// create entity if not yet defined
		EditableEntityMetaData emd = getEntityMetaData(simpleEntityName);
		for (AttributeMetaData amd : editableEntityMetaData)
		{
			emd.addAttributeMetaData(amd);
		}
	}

	public EditableEntityMetaData getEntityMetaData(String simpleEntityName)
	{
		if (!entities.containsKey(simpleEntityName))
		{
			entities.put(simpleEntityName, new DefaultEntityMetaData(simpleEntityName));
		}
		return entities.get(simpleEntityName);
	}

	/**
	 * Checks if it knows entity with given simple name.
	 * 
	 * @param name
	 *            simple name of the entity
	 * @return true if entity with simple name name is known, false otherwise
	 */
	public boolean knowsEntity(String name)
	{
		return entities.containsKey(name);
	}

	public void addPackage(String name, PackageImpl p)
	{
		packages.put(name, p);
	}

	public ImmutableMap<String, EntityMetaData> getEntityMap()
	{
		return ImmutableMap.<String, EntityMetaData> copyOf(entities);
	}

	public ImmutableList<EditableEntityMetaData> getEntities()
	{
		return ImmutableList.<EditableEntityMetaData> copyOf(entities.values());
	}

	public ImmutableMap<String, PackageImpl> getPackages()
	{
		return ImmutableMap.copyOf(packages);
	}

	public ImmutableSetMultimap<String, Tag<AttributeMetaData, LabeledResource, LabeledResource>> getAttributeTags()
	{
		return ImmutableSetMultimap.copyOf(attributeTags);
	}

	public ImmutableList<Tag<EntityMetaData, LabeledResource, LabeledResource>> getEntityTags()
	{
		return ImmutableList.copyOf(entityTags);
	}

	@Override
	public String toString()
	{
		return "ParsedMetaData [entities=" + entities + ", packages=" + packages + ", attributeTags=" + attributeTags
				+ ", entityTags=" + entityTags + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeTags == null) ? 0 : attributeTags.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((entityTags == null) ? 0 : entityTags.hashCode());
		result = prime * result + ((packages == null) ? 0 : packages.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IntermediateParseResults other = (IntermediateParseResults) obj;
		if (attributeTags == null)
		{
			if (other.attributeTags != null) return false;
		}
		else if (!attributeTags.equals(other.attributeTags)) return false;
		if (entities == null)
		{
			if (other.entities != null) return false;
		}
		else if (!entities.equals(other.entities)) return false;
		if (entityTags == null)
		{
			if (other.entityTags != null) return false;
		}
		else if (!entityTags.equals(other.entityTags)) return false;
		if (packages == null)
		{
			if (other.packages != null) return false;
		}
		else if (!packages.equals(other.packages)) return false;
		return true;
	}

	/**
	 * Gets a specific package
	 * 
	 * @param name
	 *            the name of the package
	 * @return
	 */
	public PackageImpl getPackage(String name)
	{
		return getPackages().get(name);
	}

	public Entity getTagEntity(String tagIdentifier)
	{
		return tags.get(tagIdentifier);
	}

	public void addEntityTag(TagImpl<EntityMetaData, LabeledResource, LabeledResource> tag)
	{
		entityTags.add(tag);
	}

	public void addAttributeTag(String entityName, TagImpl<AttributeMetaData, LabeledResource, LabeledResource> tag)
	{
		attributeTags.put(entityName, tag);
	}

}
