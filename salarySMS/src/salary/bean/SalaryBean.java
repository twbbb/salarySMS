package salary.bean;

public class SalaryBean
{
	String name = "";
	String preName = "";
	int num = -1;
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getPreName()
	{
		return preName;
	}
	public void setPreName(String preName)
	{
		this.preName = preName;
	}
	public int getNum()
	{
		return num;
	}
	public void setNum(int num)
	{
		this.num = num;
	}
	@Override
	public String toString()
	{
		return "SalaryBean [name=" + name + ", preName=" + preName + ", num=" + num + "]";
	}
	
	
}
