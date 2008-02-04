package de.tarent.maven.plugins.pkg;

public class AuxFile
{

  String from;
  
  String to;
  
  Boolean rename = Boolean.FALSE;

  public String getFrom()
  {
    return from;
  }

  public void setFrom(String from)
  {
    this.from = from;
  }

  public boolean isRename()
  {
    return rename.booleanValue();
  }

  public void setRename(boolean rename)
  {
    this.rename = Boolean.valueOf(rename);
  }
  
}
