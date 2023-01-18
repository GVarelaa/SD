import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

class Contact {
    private String name;
    private int age;
    private long phoneNumber;
    private String company;     // Pode ser null
    private ArrayList<String> emails;

    public Contact (String name, int age, long phoneNumber, String company, List<String> emails) {
        this.name = name;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.company = company;
        this.emails = new ArrayList<>(emails);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public long getPhoneNumber() {
        return phoneNumber;
    }

    public String getCompany() {
        return company;
    }

    public List<String> getEmails() {
        return new ArrayList(emails);
    }

    // @TODO
    // name -> 4 bytes + n bytes (4 bytes é o número de quanto bytes ocupa) (writeUTF)
    // age -> 4 bytes (writeInt)
    // phoneNumber -> 8 bytes (writeLong)
    // company ->  ? opcional (0 bytes) - boolean + valor
    // emails -> ? List<String> opcional (0 bytes) - size + values
    public void serialize (DataOutputStream out) throws IOException {
        //name
        out.writeUTF(this.name);

        //age
        out.writeInt(this.age);

        //phoneNumber
        out.writeLong(this.phoneNumber);

        //company
        if (this.company != null){
            out.writeBoolean(true);
            out.writeUTF(this.company);
        }
        else{
            out.writeBoolean(false);
        }

        //emails
        out.writeInt(this.emails.size());

        for(String email : this.emails){
            out.writeUTF(email);
        }


    }

    // @TODO
    public static Contact deserialize (DataInputStream in) throws IOException {
        String name = in.readUTF();
        int age = in.readInt();
        long phoneNumber = in.readLong();

        //company
        String company = null;
        boolean companyExists = in.readBoolean();
        if(companyExists){
            company = in.readUTF();
        }
        
        //emails
        int n_emails = in.readInt();
        ArrayList<String> emails = new ArrayList<>();
        for(int i=0 ; i < n_emails; i++){
            emails.add(in.readUTF());
        }

        return new Contact(name, age, phoneNumber, company, emails);
    }

    public String toString () {
        StringBuilder builder = new StringBuilder();
        builder.append(this.name).append(";");
        builder.append(this.age).append(";");
        builder.append(this.phoneNumber).append(";");
        builder.append(this.company).append(";");
        builder.append(this.emails.toString());
        builder.append("}");
        return builder.toString();
    }

}
