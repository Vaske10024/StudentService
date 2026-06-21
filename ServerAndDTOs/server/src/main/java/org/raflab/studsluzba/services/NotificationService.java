package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.NotificationDTO;
import org.raflab.studsluzba.model.notification.EmailOutbox;
import org.raflab.studsluzba.model.notification.Notification;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.notification.EmailOutboxRepository;
import org.raflab.studsluzba.repositories.notification.NotificationRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repo;
    private final EmailOutboxRepository outboxRepo;
    private final UserAccountRepository userRepo;
    private final CurrentUser currentUser;

    public void notifyStudent(Long indeksId, String type, String title, String message) {
        userRepo.findByLinkedStudentIndeksId(indeksId).ifPresent(u -> notify(u, type, title, message));
    }

    public Notification notify(UserAccount user, String type, String title, String message) {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification = repo.save(notification);

        String email = user.getLinkedStudentPodaci() == null ? null : user.getLinkedStudentPodaci().getEmailFakultetski();
        if (email != null) {
            EmailOutbox outbox = new EmailOutbox();
            outbox.setRecipientEmail(email);
            outbox.setSubject(title);
            outbox.setBody(message);
            outboxRepo.save(outbox);
        }
        return notification;
    }

    public List<NotificationDTO> mine() {
        return repo.findByRecipientIdOrderByCreatedAtDesc(currentUser.userId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public NotificationDTO markRead(Long id) {
        Notification notification = repo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Notifikacija ne postoji."));
        if (!notification.getRecipient().getId().equals(currentUser.userId())) {
            throw ApiException.forbidden("Notifikacija nije vasa.");
        }
        notification.setReadFlag(true);
        return toDto(repo.save(notification));
    }

    private NotificationDTO toDto(Notification notification) {
        return new NotificationDTO(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.isReadFlag(), notification.getCreatedAt());
    }
}
