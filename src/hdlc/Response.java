package hdlc;

public class Response {
	
	private ResponseType type;
	private int responseNum;
	
	public Response(ResponseType type, int frameNum) {
		
        this.type = type;
        this.responseNum = frameNum;
	}
	

}
