package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;import org.raflab.studsluzba.model.notification.*;import org.raflab.studsluzba.model.security.UserAccount;import org.raflab.studsluzba.repositories.notification.*;import org.raflab.studsluzba.repositories.security.UserAccountRepository;import org.raflab.studsluzba.security.*;import org.springframework.stereotype.Service;import java.util.List;
@Service @RequiredArgsConstructor
public class NotificationService{
 private final NotificationRepository repo;private final EmailOutboxRepository outboxRepo;private final UserAccountRepository userRepo;private final CurrentUser currentUser;
 public void notifyStudent(Long indeksId,String type,String title,String message){userRepo.findByLinkedStudentIndeksId(indeksId).ifPresent(u->notify(u,type,title,message));}
 public Notification notify(UserAccount user,String type,String title,String message){Notification n=new Notification();n.setRecipient(user);n.setType(type);n.setTitle(title);n.setMessage(message);n=repo.save(n);
  String email=user.getLinkedStudentPodaci()==null?null:user.getLinkedStudentPodaci().getEmailFakultetski();if(email!=null){EmailOutbox e=new EmailOutbox();e.setRecipientEmail(email);e.setSubject(title);e.setBody(message);outboxRepo.save(e);}return n;}
 public List<Notification> mine(){return repo.findByRecipientIdOrderByCreatedAtDesc(currentUser.userId());}
 public Notification markRead(Long id){Notification n=repo.findById(id).orElseThrow(()->ApiException.notFound("Notifikacija ne postoji."));if(!n.getRecipient().getId().equals(currentUser.userId()))throw ApiException.forbidden("Notifikacija nije vasa.");n.setReadFlag(true);return repo.save(n);}
}
