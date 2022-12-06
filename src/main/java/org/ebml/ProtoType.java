package org.ebml;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtoType<T extends Element>
{
  private static final Logger LOG = LoggerFactory.getLogger(ProtoType.class);
  private static final HashMap<Long, ProtoType<? extends Element>> CLASS_MAP = new HashMap<>();
  private final Class<T> clazz;
  private final ByteBuffer type;

  private final String name;
  private final int level;
  private long typeCode;

  public ProtoType(final Class<T> clazz, final String name, final byte[] type, final int level)
  {
    this.clazz = clazz;
    this.type = ByteBuffer.wrap(type);
    this.name = name;
    this.level = level;
    typeCode = EBMLReader.parseEBMLCode(this.type);
    CLASS_MAP.put(typeCode, this);
    LOG.trace("Associating {} with {}", name, typeCode);
  }

  public T getInstance()
  {
    LOG.trace("Instantiating {}", name);
    try
    {
      final T elem = clazz.getConstructor().newInstance();
      elem.setType(type);
      elem.setElementType(this);
      return elem;
    }
    catch (Exception e)
    {
      LOG.error("Failed to instantiate: this should never happen!", e);
      throw new RuntimeException(e);
    }
  }

  public static Element getInstance(final ByteBuffer type)
  {
    final long codename = EBMLReader.parseEBMLCode(type);
    Long codeValue = Long.valueOf(codename);
    final ProtoType<? extends Element> eType = CLASS_MAP.get(codeValue);
    if (eType == null)
    {
      System.out.printf("Unrecognized element type %s\n", Long.toHexString(codeValue));
      return null;
    }
    LOG.trace("Got codename {}, for element type {}", codename, eType.name);
    return eType.getInstance();
  }

  public String getName()
  {
    return name;
  }

  public int getLevel()
  {
    return level;
  }

  public ByteBuffer getType()
  {
    return type.asReadOnlyBuffer();
  }

  public long getTypeCode()
  {
    return typeCode;
  }
}
