package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.model.MolgenisModelException;

import java.text.ParseException;

/**
 * Created by georgebyelas on 23/03/15.
 */
public class MediumTextField extends TextField
{
    @Override
    public String getMysqlType() throws MolgenisModelException
    {
        return "MEDIUMTEXT";
    }

    @Override
    public MolgenisFieldTypes.FieldTypeEnum getEnumType()
    {
        return MolgenisFieldTypes.FieldTypeEnum.MEDIUMTEXT;
    }
}
