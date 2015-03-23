package org.molgenis.data.support;

import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.support.QueryImpl.IN;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;
import org.molgenis.util.MolgenisDateFormat;

public class DefaultEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> values = new CaseInsensitiveLinkedHashMap<Object>();
	private final EntityMetaData entityMetaData;
	private transient final DataService dataService;

	public DefaultEntity(EntityMetaData entityMetaData, DataService dataService, Map<String, Object> values)
	{
		this(entityMetaData, dataService);
		this.values.putAll(values);
	}

	public DefaultEntity(EntityMetaData entityMetaData, DataService dataService, Entity entity)
	{
		this(entityMetaData, dataService);
		set(entity);
	}

	public DefaultEntity(EntityMetaData entityMetaData, DataService dataService)
	{
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				Stream<String> atomic = stream(entityMetaData.getAtomicAttributes().spliterator(), false).map(
						a -> a.getName());
				Stream<String> compound = stream(entityMetaData.getAttributes().spliterator(), false).filter(
						a -> a.getDataType().getEnumType() == FieldTypeEnum.COMPOUND).map(a -> a.getName());

				return Stream.concat(atomic, compound).iterator();
			}
		};
	}

	@Override
	public Object getIdValue()
	{
		return get(entityMetaData.getIdAttribute().getName());
	}

	@Override
	public String getLabelValue()
	{
		return getString(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		FieldTypeEnum dataType = attribute.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
				return getBoolean(attributeName);
			case CATEGORICAL:
			case XREF:
				return getEntity(attributeName);
			case COMPOUND:
				throw new UnsupportedOperationException();
			case DATE:
				return getDate(attributeName);
			case DATE_TIME:
				return getDate(attributeName);
			case DECIMAL:
				return getDouble(attributeName);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case MEDIUMTEXT:
            case TEXT:
				return getString(attributeName);
			case FILE:
			case IMAGE:
				throw new MolgenisDataException("Unsupported data type [" + dataType + "]");
			case INT:
				return getInt(attributeName);
			case LONG:
				return getLong(attributeName);
			case MREF:
				return getEntities(attributeName);
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	@Override
	public String getString(String attributeName)
	{
		return DataConverter.toString(values.get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(values.get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(values.get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(values.get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(values.get(attributeName));
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(values.get(attributeName));
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(values.get(attributeName));
	}

	@Override
	public java.sql.Date getDate(String attributeName)
	{
		java.util.Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		Object value = values.get(attributeName);
		if (value == null) return null;
		if (value instanceof java.util.Date) return (java.util.Date) value;

		try
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			FieldTypeEnum dataType = attribute.getDataType().getEnumType();
			switch (dataType)
			{
				case DATE:
					return MolgenisDateFormat.getDateFormat().parse(value.toString());
				case DATE_TIME:
					return MolgenisDateFormat.getDateTimeFormat().parse(value.toString());
					// $CASES-OMITTED$
				default:
					throw new MolgenisDataException("Type [" + dataType + "] is not a date type");

			}
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		java.util.Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new Timestamp(utilDate.getTime()) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entity getEntity(String attributeName)
	{
		Object value = values.get(attributeName);
		if (value == null) return null;
		if (value instanceof Entity) return (Entity) value;

		// value represents the id of the referenced entity
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		if (value instanceof Map) return new DefaultEntity(attribute.getRefEntity(), dataService,
				(Map<String, Object>) value);

		value = attribute.getDataType().convert(value);
		Entity refEntity = dataService.findOne(attribute.getRefEntity().getName(), value);
		if (refEntity == null) throw new UnknownEntityException(attribute.getRefEntity().getName() + " with "
				+ attribute.getRefEntity().getIdAttribute().getName() + " [" + value + "] does not exist");

		return refEntity;
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		Entity entity = getEntity(attributeName);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity), dataService).iterator().next() : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		List<?> ids;
		Object value = values.get(attributeName);
		if (value instanceof String) ids = getList(attributeName);
		else ids = (List<?>) value;

		if ((ids == null) || ids.isEmpty()) return Collections.emptyList();
		if (ids.get(0) instanceof Entity) return (Iterable<Entity>) ids;

		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		if (ids.get(0) instanceof Map)
		{
			return stream(ids.spliterator(), false).map(
					id -> new DefaultEntity(attribute.getRefEntity(), dataService, (Map<String, Object>) id)).collect(
					Collectors.toList());
		}

		EntityMetaData ref = attribute.getRefEntity();
		ids = ids.stream().map(attribute.getDataType()::convert).collect(Collectors.toList());

		return dataService.findAll(ref.getName(), IN(ref.getIdAttribute().getName(), ids));
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		Iterable<Entity> entities = getEntities(attributeName);
		return entities != null ? new ConvertingIterable<E>(clazz, entities, dataService) : null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		// XRefs are stores as id in the values map, MRef as list of id
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		if ((attribute.getDataType() instanceof XrefField) && (value instanceof Entity))
		{
			Entity refEntity = (Entity) value;
			values.put(attributeName, refEntity.getIdValue());
		}
		else if ((attribute.getDataType() instanceof MrefField) && (value instanceof Iterable<?>))
		{
			List<?> ids = stream(((Iterable<?>) value).spliterator(), false).map(
					v -> v instanceof Entity ? ((Entity) v).getIdValue() : v).collect(Collectors.toList());
			values.put(attributeName, ids);
		}
		else
		{
			values.put(attributeName, value);
		}
	}

	@Override
	public void set(Entity entity)
	{
		entityMetaData.getAtomicAttributes().forEach(attr -> set(attr.getName(), entity.get(attr.getName())));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityMetaData == null) ? 0 : entityMetaData.hashCode());
		result = prime * result + ((getIdValue() == null) ? 0 : getIdValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Entity)) return false;
		Entity other = (Entity) obj;

		if (entityMetaData == null)
		{
			if (other.getEntityMetaData() != null) return false;
		}
		else if (!entityMetaData.equals(other.getEntityMetaData())) return false;
		if (getIdValue() == null)
		{
			if (other.getIdValue() != null) return false;
		}
		else if (!getIdValue().equals(other.getIdValue())) return false;
		return true;
	}

}
