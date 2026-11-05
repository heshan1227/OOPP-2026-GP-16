package fams.TG_2024_2113;

import fams.TG_2024_2133.Role;
import fams.TG_2024_2133.User;
import fams.TG_2024_2133.UnauthorizedAccessException;

/** Central access policy for marks/results operations. Technical Officers are denied every result operation. */
public final class ResultsAccessPolicy {
    private ResultsAccessPolicy() {}
    public static void requireResultsRole(User user) throws UnauthorizedAccessException {
        if(user==null) throw new UnauthorizedAccessException("Please log in.");
        if(user.getRole()==Role.TECHNICAL_OFFICER)
            throw new UnauthorizedAccessException("Technical Officers do not have access to the results module.");
    }
    public static void requireStudentOwnRecord(User user,String requestedUserId) throws UnauthorizedAccessException {
        requireResultsRole(user);
        if(user.getRole()==Role.UNDERGRADUATE && !user.getUserId().equals(requestedUserId))
            throw new UnauthorizedAccessException("You can only access your own approved results.");
    }
    public static void requireLecturerCourse(User user,boolean assigned) throws UnauthorizedAccessException {
        requireResultsRole(user);
        if(user.getRole()==Role.LECTURER && !assigned)
            throw new UnauthorizedAccessException("You are not assigned to this course.");
        if(user.getRole()!=Role.LECTURER && user.getRole()!=Role.ADMIN)
            throw new UnauthorizedAccessException("This operation is restricted to lecturers and administrators.");
    }
}
