import java.util.ArrayList;
import java.util.List;


public class HTTPHeader {
	List<HTTPHeaderField> fields;
	String firstLine;
	
	public HTTPHeader(String header) {
		fields = new ArrayList<HTTPHeaderField>();
		
		String[] headerParts = header.replaceAll("\r", "").split("\n");
		
		for(int i = 0; i < headerParts.length; i++) {
			if(!headerParts[i].equals("")) {
				if(headerParts[i].split(" ")[0].equals("GET")) {
					firstLine = headerParts[i];
				}
				else
					fields.add(new HTTPHeaderField(headerParts[i]));
			}
		}
	}
	
	public HTTPHeader(List<String> header) {
		fields = new ArrayList<HTTPHeaderField>();
		
		for(int i = 0; i < header.size(); i++) {
			String part = header.get(i).replaceAll("\r", "").replaceAll("\n", "");
			
			if(!part.equals("")) {
				if(part.startsWith("GET")) {
					firstLine = part;
				}
				else
					fields.add(new HTTPHeaderField(part));
			}
		}
	}
	
	public HTTPHeader() {
		fields = new ArrayList<HTTPHeaderField>();
		firstLine = "GET / HTTP/1.1";
	}
	
	@Override
	public String toString() {
		StringBuilder header = new StringBuilder();
		
		header.append(firstLine);
		header.append("\n");
		
		for(int i = 0; i < fields.size(); i++) {
			header.append(fields.get(i).toString());
		}
		
		return header.toString();
	}

	public void setFirstLine(String firstLine) {
		this.firstLine = firstLine;
	}

	public void addField(HTTPHeaderField toAdd) {
		fields.add(toAdd);
	}

	public HTTPHeaderField getField(String name) {
		for(int i = 0; i < fields.size(); i++) {
			if(fields.get(i).getName().equals(name))
				return fields.get(i);
		}
		return null;
	}
}

class HTTPHeaderField {
	private String value, name;
	
	public HTTPHeaderField(String data) {
		String parts[] = data.split(": ");
		
		if(parts.length != 2) {
			System.out.println("Wrong length of headerfield !! (" + parts.length + ")");
			CommonUsedFeatures.printArray(parts);
		}
		
		name = parts[0];
		value = parts[1];
	}
	
	public HTTPHeaderField(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	String getName() {
		return name;
	}
	
	String getValue() {
		return value;
	}
	
	void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return name + ": " + value + "\n";
	}
}